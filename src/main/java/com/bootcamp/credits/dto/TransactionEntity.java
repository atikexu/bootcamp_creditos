package com.bootcamp.credits.dto;

import com.bootcamp.credits.enums.ProductType;
import com.bootcamp.credits.enums.TransactionType;
import java.util.Date;
import lombok.Data;

@Data
public class TransactionEntity {
    private String id;
    private ProductType productType;  // AHORRO, C_CORRIENTE, PLAZO_FIJO, CRE_PERSONAL, CRED_EMPRESARIAL, TAR_CRED_PERSONAL, TAR_CRED_EMPRESARIAL
    private String productId;
    private String customerId;
    private TransactionType transactionType;  // DEPOSITO, RETIRO, PAGO, CONSUMO
    private Double amount;
    private Date transactionDate;

}
