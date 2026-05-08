package com.signflow.enums;

/**
 * Canais de notificação disponíveis para entrega do link de assinatura.
 * <p>
 * Cada gateway mapeia para o modelo especifico do provider.
 * <p>
 * ClickSign:
 *   EMAIL -> delivery: "email" e requestSignature: "email"
 *   SMS -> delivery: "sms" e requestSignature: "sms"
 *   WHATSAPP -> delivery: "whatsapp" e requestSignature: "whatsapp"
 * <p>
 * DocuSign (futuro):
 *   EMAIL -> deliveryMethod: "email"
 *   SMS -> deliveryMethod: "sms"
 *   etc.
 */
public enum NotificationChannel {

    EMAIL,SMS,WHATSAPP
}
