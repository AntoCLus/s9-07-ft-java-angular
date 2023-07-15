package com.nocountry.ecommerce.service.apipayment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.nocountry.ecommerce.dto.Mensaje;
import com.nocountry.ecommerce.model.Orders;
import com.nocountry.ecommerce.model.Pay;
import com.nocountry.ecommerce.repository.PayRepository;
import com.nocountry.ecommerce.util.enums.TransactionState;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.CreateTransactionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChargeCreditCard {
    @Autowired
    private PayRepository payRepository;
    @Value("${spring.net.authorize.loginapi}")
    String loginId;
    @Value("${spring.net.authorize.transactionkey}")
    String transactionKey;
    public ANetApiResponse run(Pay pay, Double total) {

        // Set the request to operate in either the sandbox or production environment
        ApiOperationBase.setEnvironment(Environment.SANDBOX);

        // Create object with merchant authentication details
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Populate the payment data
        Pay savePayment = new Pay();

        String cardNumber = pay.getCardNumber();
        savePayment.setCardNumber(cardNumber);

        String expirationDate = pay.getExpirationDate();
        savePayment.setExpirationDate(expirationDate);

        String cardCode = pay.getCardCode();
        savePayment.setCardCode(cardCode);

        savePayment.setTransactionState(TransactionState.COMPLETED);

        Orders orders = pay.getOrders();
        savePayment.setOrders(orders);

        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(cardNumber);
        creditCard.setExpirationDate(expirationDate);
        creditCard.setCardCode(cardCode);
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setPayment(paymentType);
        txnRequest.setAmount(new BigDecimal(total).setScale(2, RoundingMode.CEILING));

        // Create the API request and set the parameters for this specific request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setMerchantAuthentication(merchantAuthenticationType);
        apiRequest.setTransactionRequest(txnRequest);

        // Call the controller
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();


        // Get the response
        CreateTransactionResponse response = new CreateTransactionResponse();
        response = controller.getApiResponse();

        if (response != null) {

            // Parse the response to determine results
            if (response != null) {
                // If API Response is OK, go ahead and check the transaction response
                if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                    TransactionResponse result = response.getTransactionResponse();
                    if (result.getMessages() != null) {
                        System.out.println("Successfully created transaction with Transaction ID: " + result.getTransId());
                        System.out.println("Response Code: " + result.getResponseCode());
                        System.out.println("Message Code: " + result.getMessages().getMessage().get(0).getCode());
                        System.out.println("Description: " + result.getMessages().getMessage().get(0).getDescription());
                        System.out.println("Auth Code: " + result.getAuthCode());
                        payRepository.save(savePayment);
                    } else {
                        System.out.println("Failed Transaction.");
                        if (response.getTransactionResponse().getErrors() != null) {
                            System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                            System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                        }
                    }
                } else {
                    System.out.println("Failed Transaction.");
                    if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                        System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    } else {
                        System.out.println("Error Code: " + response.getMessages().getMessage().get(0).getCode());
                        System.out.println("Error message: " + response.getMessages().getMessage().get(0).getText());
                    }
                }
            } else {
                // Display the error code and message when response is null
                ANetApiResponse errorResponse = controller.getErrorResponse();
                System.out.println("Failed to get response");
                if (!errorResponse.getMessages().getMessage().isEmpty()) {
                    System.out.println("Error: " + errorResponse.getMessages().getMessage().get(0).getCode() + " \n" + errorResponse.getMessages().getMessage().get(0).getText());
                }
            }
        }
        return response;
    }
}