package com.inn.restaurant.service;

import com.inn.restaurant.model.Bill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
public interface BillService {
    ResponseEntity<String> generateReport(Map<String, Object> requestMap);

    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<String> deleteBill(Integer id);

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);
}
