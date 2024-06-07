package com.inn.restaurant.serviceImpl;

import com.inn.restaurant.JWT.JwtFilter;
import com.inn.restaurant.constants.RestaurantConstants;
import com.inn.restaurant.dao.BillDao;
import com.inn.restaurant.model.Bill;
import com.inn.restaurant.service.BillService;
import com.inn.restaurant.utils.RestaurantUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;


    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");
        try {
            String fileName;
            if(validateRequestMap(requestMap)) {
                if(requestMap.containsKey("isGenerate") && !(Boolean)requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid"); // if the file is already in the database it will pass this uuid from inside
                    // requestMap
                } else {
                    fileName = RestaurantUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }
                String data = "Name: " + requestMap.get("name") + "\n" +
                        "ContactNumber: " +requestMap.get("contactNumber") + "\n" +
                        "Email: " + requestMap.get("email") + "\n" +
                        "Payment Method: " + requestMap.get("paymentMethod") + "\n";
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(RestaurantConstants.STORE_LOCATION+"\\"+fileName+".pdf"));

                document.open();
                setRectangleInPdf(document);

                // Add header section
                Paragraph header = new Paragraph();
                header.setAlignment(Element.ALIGN_CENTER);
                header.add(new Phrase("Casa Sirca\n\n", getFont("Header")));
                header.add(new Phrase("Strada Principala 2B\nBoghis, Salaj, Romania\n", getFont("Subheader")));
                header.add(new Phrase("Bill Number: " + fileName + "\nDate: " + RestaurantUtils.getCurrentDate() + "\n\n", getFont("Subheader")));
                document.add(header);


                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = RestaurantUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for(int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, RestaurantUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);
                double totalAmount = 0.0;
                for (int i = 0; i < table.getRows().size(); i++) {
                    PdfPCell[] cells = table.getRows().get(i).getCells();
                    if (cells.length > 0) {
                        // Assuming the subtotal is in the last cell of each row
                        PdfPCell lastCell = cells[cells.length - 1];
                        String content = lastCell.getPhrase().getContent();
                        try {
                            // Check if the content is "Sub Total", if so, skip parsing
                            if (!content.equalsIgnoreCase("Sub Total")) {
                                double subtotal = Double.parseDouble(content);
                                totalAmount += subtotal;
                            }
                        } catch (NumberFormatException e) {
                            // Handle the case where content is not a valid numeric string
                            log.error("Invalid subtotal: {}", content);
                        }
                    }
                }


                Paragraph footer = new Paragraph();
                footer.setAlignment(Element.ALIGN_CENTER);
                footer.add(new Phrase("\nTotal : " + totalAmount + "\n", getFont("Data")));
                footer.add(new Phrase("Thank you for choosing us!\n We sincerely appreciate your visit and hope to welcome you back soon.\n", getFont("Footer")));
                footer.add(new Phrase("For inquiries, call: 0740385288\nEmail: casasirca@gmail.com", getFont("Footer")));
                document.add(footer);
                document.close();
                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            }
            return RestaurantUtils.getResponseEntity("Required Data Not Found!", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails");
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));

            // Calculate total amount from product details
            JSONArray jsonArray = RestaurantUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
            double totalAmount = 0.0; // Initialize total amount
            for(int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> product = RestaurantUtils.getMapFromJson(jsonArray.getString(i));
                double quantity = Double.parseDouble(product.get("quantity").toString());
                double price = Double.parseDouble(product.get("price").toString());
                totalAmount += quantity * price; // Calculate subtotal and add to totalAmount
            }
            bill.setTotal((int) totalAmount); // Assuming total amount is stored as integer

            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
            // Handle the exception appropriately
        }
    }



    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.CYAN);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type) {
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.GREEN);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Subheader":
                Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
                subheaderFont.setStyle(Font.BOLD);
                return subheaderFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.COURIER, 11, BaseColor.BLACK);
                return dataFont;
            case "Footer":
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
                return footerFont;
            default:
                return new Font();
        }
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader.");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));

        // Convert quantity and price to strings
        String quantityStr = String.valueOf(data.get("quantity"));
        String priceStr = String.valueOf(data.get("price"));

        // Check if quantity and price are numeric
        double quantity = 0.0;
        double price = 0.0;
        try {
            quantity = Double.parseDouble(quantityStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            // Handle non-numeric quantity or price gracefully
            log.error("Quantity or price is not a valid number.");
        }

        // Calculate subtotal
        double subtotal = quantity * price;

        // Add cells to the table
        table.addCell(quantityStr); // Add quantity as is, whether numeric or not
        table.addCell(priceStr);    // Add price as is, whether numeric or not
        table.addCell(String.valueOf(subtotal)); // Add subtotal
    }
    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = new ArrayList<>();
        if(jwtFilter.isAdmin()) {
            list = billDao.getAllBills();
        } else {
            list = billDao.getBillByUsername(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional optional = billDao.findById(id);
            if(!optional.isEmpty()) {
                billDao.deleteById(id);
                return RestaurantUtils.getResponseEntity("Bill Deleted Successfully!", HttpStatus.OK);
            }
            return RestaurantUtils.getResponseEntity("Bill Id doesn't exist!", HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filePath) throws Exception{
        File initialFile = new File(filePath);
        InputStream targetStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf: request map {}", requestMap);
        try {
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)) {
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = RestaurantConstants.STORE_LOCATION + "\\" + (String) requestMap.get("uuid") + ".pdf";
            if(RestaurantUtils.doesFileExist(filePath)) {
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            } else {
                requestMap.put("idGenerated", false);
                generateReport(requestMap);
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}