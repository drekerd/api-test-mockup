package com.drekerd.apitestmockup;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Log4j2
@RestController
public class Login {

    private static final String API_SECRET = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    @GetMapping("/authenticate")
    public String getJWT(@RequestBody User user){
        log.info("Authenticate ENTER", user.getId());
        return createJWT(user);
    }

    private String createJWT(User user){

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(API_SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        JwtBuilder jwt = Jwts.builder().setId(Long.valueOf(user.getId()).toString())
                .setIssuedAt(now)
                .setSubject(user.getRole())
                .setIssuer("http://trustyapp")
                .signWith(signatureAlgorithm, signingKey);

            long expMillis = nowMillis + 120000;
            Date exp = new Date(expMillis);
            jwt.setExpiration(exp);

        return jwt.compact();
    }
}
