package com.akinobank.app.utilities;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CreditCardNumberValidator implements ConstraintValidator<CreditCardNumber, String> {

    @Override
    public boolean isValid(String numeroCompte, ConstraintValidatorContext context) {
        System.out.println("VALIDATING CCN");
        boolean isValid = false;

        try {
            String reversedNumber = new StringBuffer(numeroCompte)
                .reverse().toString();
            int mod10Count = 0;
            for (int i = 0; i < reversedNumber.length(); i++) {
                int augend = Integer.parseInt(String.valueOf(reversedNumber
                    .charAt(i)));
                if (((i + 1) % 2) == 0) {
                    String productString = String.valueOf(augend * 2);
                    augend = 0;
                    for (int j = 0; j < productString.length(); j++) {
                        augend += Integer.parseInt(String.valueOf(productString
                            .charAt(j)));
                    }
                }

                mod10Count += augend;
            }

            if ((mod10Count % 10) == 0) {
                isValid = true;
            }
        } catch (NumberFormatException e) {}
        return isValid;
    }

    @Override
    public void initialize(CreditCardNumber creditCardNumber) {}

}
