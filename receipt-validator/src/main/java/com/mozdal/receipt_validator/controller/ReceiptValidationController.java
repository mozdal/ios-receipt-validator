package com.mozdal.receipt_validator.controller;

import com.mozdal.receipt_validator.service.ReceiptValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RequestMapping("/api/v1/validator")
@RestController
@CrossOrigin
public class ReceiptValidationController {

    @Autowired
    ReceiptValidationService receiptValidationService;

    @RequestMapping(value = "/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity validate(@RequestBody final String receipt) throws Exception {

        HashMap<String, Object> result = receiptValidationService.validateReceipt(receipt);
        Boolean isReceiptVerified = (Boolean)result.get("result");

        if(isReceiptVerified) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
