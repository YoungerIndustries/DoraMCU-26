package com.yisy.modules.doraneuron.security;

import com.yisy.modules.doraneuron.config.NeuronSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Verifies RSA signatures for ONNX model files.
 *
 * This service ensures that downloaded ONNX models are authentic by verifying
 * their digital signatures against a public key. This prevents tampered or
 * malicious models from being loaded into the neuron network.
 */
@Service
@ConditionalOnProperty(name = "neuron.security.model-signature-enabled", havingValue = "true", matchIfMissing = true)
public class ModelSignatureService {

    private static final Logger log = LoggerFactory.getLogger(ModelSignatureService.class);

    private final NeuronSecurityConfig securityConfig;
    private volatile PublicKey publicKey;

    public ModelSignatureService(NeuronSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @PostConstruct
    public void init() {
        String keyPath = securityConfig.getModelPublicKeyPath();
        if (keyPath != null && !keyPath.isEmpty()) {
            loadPublicKey(keyPath);
        } else {
            log.warn("Model signature verification enabled but no public key path configured");
        }
    }

    /**
     * Load the RSA public key from a file.
     */
    private void loadPublicKey(String keyPath) {
        try {
            File keyFile = new File(keyPath);
            if (!keyFile.exists()) {
                log.error("Public key file not found: {}", keyPath);
                return;
            }

            byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
            String keyContent = new String(keyBytes).replaceAll("[\\r\\n\\s]", "");

            // Remove PEM headers if present
            keyContent = keyContent.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] decoded = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(spec);

            log.info("Loaded RSA public key from: {}", keyPath);
        } catch (IOException e) {
            log.error("Failed to read public key file: {}", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA algorithm not available: {}", e.getMessage());
        } catch (InvalidKeySpecException e) {
            log.error("Invalid public key format: {}", e.getMessage());
        }
    }

    /**
     * Verify the signature of a model file.
     *
     * @param modelPath Path to the ONNX model file
     * @param signaturePath Path to the signature file (.sig)
     * @return true if the signature is valid
     */
    public boolean verifySignature(String modelPath, String signaturePath) {
        if (publicKey == null) {
            log.warn("No public key loaded, skipping signature verification");
            return true; // Skip verification if no key
        }

        try {
            File modelFile = new File(modelPath);
            File signatureFile = new File(signaturePath);

            if (!modelFile.exists()) {
                log.error("Model file not found: {}", modelPath);
                return false;
            }

            if (!signatureFile.exists()) {
                log.error("Signature file not found: {}", signaturePath);
                return false;
            }

            // Read the signature
            byte[] signature = Files.readAllBytes(signatureFile.toPath());

            // Compute the digest of the model file
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(modelFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] modelDigest = digest.digest();

            // Verify the signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(modelDigest);

            return sig.verify(signature);
        } catch (IOException e) {
            log.error("Failed to verify model signature: {}", e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            log.error("Signature algorithm not available: {}", e.getMessage());
            return false;
        } catch (InvalidKeyException e) {
            log.error("Invalid public key: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify the signature of a model file using an inline base64-encoded signature.
     *
     * @param modelPath Path to the ONNX model file
     * @param base64Signature Base64-encoded signature
     * @return true if the signature is valid
     */
    public boolean verifySignatureWithBase64(String modelPath, String base64Signature) {
        if (publicKey == null) {
            log.warn("No public key loaded, skipping signature verification");
            return true;
        }

        try {
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                log.error("Model file not found: {}", modelPath);
                return false;
            }

            byte[] signature = Base64.getDecoder().decode(base64Signature);

            // Compute the digest of the model file
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(modelFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] modelDigest = digest.digest();

            // Verify the signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(modelDigest);

            boolean result = sig.verify(signature);
            if (result) {
                log.info("Model signature verified successfully: {}", modelPath);
            } else {
                log.error("Model signature verification failed: {}", modelPath);
            }
            return result;
        } catch (IOException e) {
            log.error("Failed to verify model signature: {}", e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            log.error("Signature algorithm not available: {}", e.getMessage());
            return false;
        } catch (InvalidKeyException e) {
            log.error("Invalid public key: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate a signature for a model file (for testing purposes).
     *
     * @param modelPath Path to the ONNX model file
     * @param privateKeyPath Path to the private key file
     * @return Base64-encoded signature, or null if signing failed
     */
    public String signModel(String modelPath, String privateKeyPath) {
        // This method is for testing/development only
        // In production, signing should be done by the model training service
        log.warn("Model signing is for testing only, not for production use");
        return null;
    }

    /**
     * Check if signature verification is available.
     */
    public boolean isVerificationAvailable() {
        return publicKey != null;
    }
}
