package com.porotype.totpassword;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import fi.jasoft.qrcode.QRCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.vaadin.maddon.button.PrimaryButton;
import org.vaadin.maddon.label.RichText;
import org.vaadin.maddon.layouts.MHorizontalLayout;
import org.vaadin.maddon.layouts.MVerticalLayout;

/**
 * @author Marc Englund
 *
 */
@SuppressWarnings("serial")
@Theme("dawn")
public class TOTPasswordUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = TOTPasswordUI.class, widgetset = "com.porotype.totpassword.widgetset.TotpasswordWidgetset")
    public static class Servlet extends VaadinServlet {
    }

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Override
    protected void init(final VaadinRequest request) {

        /*
         * 1. Setup
         */
        // Generate secret key, display as QR code and as a plaintext link
        final GoogleAuthenticatorKey key = gAuth.createCredentials();

        // make the URI
        String keyUri = "";
        try {
            keyUri = generateKeyUri("totp@example.com", "Vaadin TOTP",
                    key.getKey());
        } catch (URISyntaxException e) {
            Notification.show("Could not generate QR code", Type.ERROR_MESSAGE);
        }

        // generate QR code
        final QRCode sharedKeyQrCode = new QRCode();
        sharedKeyQrCode.setValue(keyUri);
        sharedKeyQrCode.setWidth("140px");
        sharedKeyQrCode.setHeight("140px");

        // add a key in plain text, as clickable link
        final Label sharedKeyText = new Label(
                "<h2><a href=\"" + keyUri + "\">" + key.getKey()
                + "</a></h2>", ContentMode.HTML);

        /*
         * 2. Verification
         */
        final TextField password = new TextField("Password");
        password.setInputPrompt("123456");
        password.setColumns(6);

        Button check = new PrimaryButton("Check", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                String value = password.getValue();
                Integer totp = Integer.
                        valueOf((value.equals("") ? "-1" : value));

                // it's a good idea to allow each password only once
                boolean unused = isUnusedPassword(totp, gAuth.getWindowSize());

                // verify the password
                boolean matches = gAuth.authorize(key.getKey(), totp);

                if (unused && matches) {
                    Notification.show("Correct");

                } else {
                    Notification.show("Incorrect :-(",
                            Notification.Type.WARNING_MESSAGE);
                }

            }
        });

        setContent(
                new MVerticalLayout(
                        new RichText().withMarkDownResource("/hello.md"),
                        new MHorizontalLayout(sharedKeyQrCode, sharedKeyText),
                        new MHorizontalLayout(password, check)
                            .withHeight("100px").alignAll(Alignment.BOTTOM_CENTER)
                )
        );

    }

    /**
     * Generates a URI that Google Authenticator and most TOTP apps can read
     * when displayed as a QR code.
     * <p>
     * The format is:
     * otpauth://totp/[ISSUER]:[ACCOUNT]?secret=[SECRET]&issuer=[ISSUER]
     * </p>
     *
     * @see https://code.google.com/p/google-authenticator/wiki/KeyUriFormat
     *
     * @param account account/username
     * @param issuer the provider or service this account is associated with
     * @param secret shared secret key
     * @return URI string
     * @throws URISyntaxException
     */
    private static String generateKeyUri(String account, String issuer,
            String secret) throws URISyntaxException {

        URI uri = new URI("otpauth", "totp", "/" + issuer + ":" + account,
                "secret=" + secret + "&issuer=" + issuer, null);

        return uri.toASCIIString();
    }

    /*
     * Bookkeeping for used passwords follow. Imperfect example implementation;
     * you would store this in the database, for instance with the shared key.
     */
    int lastUsedPassword = -1; // last successfully used password
    private long lastVerifiedTime = 0; // time of last success

    /**
     * Simplified implementation that ensures each password is only used once,
     * by making sure the provided password has not previously been used within
     * the allowed timeframe. Note that the same password can certainly be valid
     * in the future, we just want to prevent using it again right away. <br/>
     * <b>This implementation is not perfect.</b>
     *
     * @param password the password being verified
     * @param windowSize number of intervals being checked
     * @return
     */
    private boolean isUnusedPassword(int password, int windowSize) {
        long now = new Date().getTime();
        long timeslotNow = now / GoogleAuthenticator.KEY_VALIDATION_INTERVAL_MS;
        long timeslotThen = lastVerifiedTime
                / GoogleAuthenticator.KEY_VALIDATION_INTERVAL_MS;

        int forwardTimeslots = ((windowSize - 1) / 2);

        if (password != lastUsedPassword
                || timeslotNow > timeslotThen + forwardTimeslots) {
            lastUsedPassword = password;
            lastVerifiedTime = now;
            return true;
        }

        return false;
    }

}
