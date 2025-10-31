import java.sql.*;
import java.sql.Date;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.Scanner;
import java.io.*;

class Customer {

    static HashMap<String, String> customerData = new HashMap<>();
    static Connection con;
    public Customer() throws Exception{
        connection();
        loadHashMap();
        login();
    }
    static void connection()throws Exception{
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/electricity_bill_management","root","Vinod@123");
        if(con != null)
        {
//            System.out.println("connected");
        }
        else
        {
//            System.out.println("Not connected");
        }
    }

    public static void loadHashMap() {
        try {
            customerData.clear();
            String query = "SELECT service_number, password FROM customers";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String serviceNo = rs.getString("service_number");
                String password = rs.getString("password");

                customerData.put(serviceNo, password);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static void login() throws Exception{
        Scanner sc = new Scanner(System.in);
        boolean loggedIn = false;

        System.out.println("------ Customer Portal ------");
        System.out.println("Login");
        while (!loggedIn) {
            System.out.print("Enter Service Number : ");
            String serviceNumber = sc.next();
            sc.nextLine();

            if (customerData.containsKey(serviceNumber)) {
                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                if (customerData.get(serviceNumber).equals(password)) {

                    String selectName="SELECT name FROM customers WHERE service_number = ?";
                    PreparedStatement ps = con.prepareStatement(selectName);
                    ps.setString(1,serviceNumber);
                    ResultSet rs = ps.executeQuery();
                    String name="";
                    while (rs.next()){
                        name=rs.getString(1);
                    }
                    System.out.println("Login successful! Welcome, " + name + ".");

                    loggedIn = true;
                    consumerMenu(serviceNumber);
                } else {
                    System.out.println("Incorrect password. Try again.\n");
                }

            } else {

                System.out.println("Service number not found. Do you want to register as a new customer? (yes/no)");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("yes")) {

                    String selectName="SELECT MAX(service_number) FROM customers";
                    PreparedStatement ps = con.prepareStatement(selectName);
                    ResultSet rs = ps.executeQuery();
                    String newNumber = "";
                    while (rs.next())
                    {
                        newNumber = rs.getString(1);
                    }
                    int a = Integer.parseInt(newNumber) + 1;
                    newNumber = String.valueOf(a);

                    System.out.print("Enter your name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter address: ");
                    String address = sc.nextLine();

                    System.out.print("Enter email: ");
                    String email = sc.nextLine();

                    boolean flag = true;
                    String phone = "";
                    while (flag) {
                        System.out.print("Enter phone: ");
                        phone = sc.nextLine();

                        if(phone.length() == 10)
                        {
                            for(int i = 0; i < phone.length(); i++)
                            {
                                char a1 = phone.charAt(i);
                                if(Character.isDigit(a1))
                                {
                                    flag = false;
                                    continue;

                                }
                                else
                                {
                                    System.out.println("Invalid Input!!");
                                    flag = true;
                                    break;
                                }
                            }

                        }
                        else
                        {
                            System.out.println("Please enter a 10 digit number!!");
                        }
                    }

                    String type = "";
                    while (true) {
                        System.out.print("Enter Connection Type (Domestic/Commercial/Industrial): ");
                        type = sc.nextLine();

                        if(type.equalsIgnoreCase("Domestic") || type.equalsIgnoreCase("Commercial") || type.equalsIgnoreCase("Industrial"))
                        {
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid Input!!");
                        }
                    }

                    String date = "";
                    while (true) {
                        System.out.print("Enter Connection Date (YYYY-MM-DD): ");
                        date = sc.nextLine();

                        if (isValidDate(date)) {
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid Date!!!");
                        }
                    }

                    System.out.print("Set your password: ");
                    String newPassword = sc.nextLine();

                    try {
                        String query = "INSERT INTO customers (service_number, name, address, contact_number, email, connection_type, connection_date, password) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement pst = con.prepareStatement(query);
                        pst.setString(1, newNumber); // service_number is VARCHAR
                        pst.setString(2, name);
                        pst.setString(3, address);
                        pst.setString(4, phone);
                        pst.setString(5, email);
                        pst.setString(6, type);
                        pst.setString(7, date);
                        pst.setString(8, newPassword);

                        pst.executeUpdate();

                        customerData.put(newNumber, newPassword);
                        System.out.println("Registration successful! You can now log in.\n");
                        System.out.println("Your service number is : "+newNumber);


                    } catch (SQLException e) {
                        System.out.println("Registration failed: " + e.getMessage());
                    }
                }
                else if(choice.equalsIgnoreCase("No")){
                    System.out.println("Returning to login...\n");
                }
                else
                {
                    System.out.println("Invalid Input!!");
                }
            }
        }
    }

    public static void consumerMenu(String serviceNumber) {
        Scanner sc = new Scanner(System.in);


        int choice;
        boolean flag=true;
        while(flag) {
            System.out.println("\n====== Consumer Panel ======");
            System.out.println("1. View Current Bill");
            System.out.println("2. Pay Bill");
            System.out.println("3. View Bill Payment History");
            System.out.println("4. Register a Complaint");
            System.out.println("5. View Complaint Status");
            System.out.println("6. Download Bill Receipt");
            System.out.println("7. Give Feedback");
            System.out.println("8. Pay Advance Payment and Get Discount");
            System.out.println("9. Logout");
            System.out.print("Enter your choice: ");

            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();

            } else {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    viewCurrentBill(serviceNumber);
                    break;
                case 2:
                    payBill(serviceNumber);
                    break;
                case 3:
                    viewBillHistoryConsumer(serviceNumber);
                    break;
                case 4:
                    System.out.print("Enter complaint text: ");
                    String complaintText = sc.next();
                    registerComplaint(serviceNumber, complaintText);
                    break;
                case 5:
                    viewComplaintStatus(serviceNumber);
                    break;
                case 6:
                    downloadBillReceipt(serviceNumber);
                    break;
                case 7:
                    receiveFeedback(serviceNumber);
                    break;
                case 8:
                    advancePayment(serviceNumber);
                    break;
                case 9:
                    flag=false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }


    static void viewCurrentBill(String serviceNumber) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM bills WHERE service_number = ? AND status = 'Unpaid' ORDER BY billing_date DESC LIMIT 1";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, serviceNumber);

            rs = pstmt.executeQuery();


            System.out.println("\n----- Current Pending Bills -----");

            double totalAmount = 0.0;
            boolean hasBills = false;

            while (rs.next()) {
                hasBills = true;
                System.out.println("--------------------------------------------------");
                System.out.println("Month           : " + rs.getString("billing_date"));
                System.out.println("Units Consumed  : " + rs.getInt("total_units"));
                System.out.println("Amount          : ₹" + rs.getDouble("amount"));
                System.out.println("Status          : " + rs.getString("status"));
                totalAmount += rs.getDouble("amount");
            }

            if (hasBills) {
                System.out.println("--------------------------------------------------");
                System.out.println("Total Payable Amount : ₹" + totalAmount);
            } else {
                System.out.println("No unpaid bills found for Service Number: " + serviceNumber);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.out.println( e.getMessage());
            }
        }
    }


    static void payBill(String serviceNumber) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Scanner sc = new Scanner(System.in);
        try {
            String query = "SELECT * FROM bills WHERE service_number = ? AND status = 'Unpaid' ORDER BY billing_date DESC";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, serviceNumber);
            rs = pstmt.executeQuery();

            double totalAmount = 0.0;
            boolean hasUnpaid = false;

            List<Integer> billIds = new ArrayList<>();
            List<Double> amounts = new ArrayList<>();
            List<Double> units = new ArrayList<>();
            List<String> dates = new ArrayList<>();

            System.out.println("\n----- Unpaid Bills -----");

            while (rs.next()) {
                hasUnpaid = true;
                System.out.println("--------------------------------------------------");
                System.out.println("Month           : " + rs.getString("billing_date"));
                System.out.println("Units Consumed  : " + rs.getDouble("total_units"));
                System.out.println("Amount          : ₹" + rs.getDouble("amount"));
                totalAmount += rs.getDouble("amount");
                billIds.add(rs.getInt("bill_id"));
                amounts.add(rs.getDouble("amount"));
                units.add(rs.getDouble("total_units"));
                dates.add(rs.getString("billing_date"));
            }


            if (!hasUnpaid) {
                System.out.println("No pending bills to pay.");
                return;
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Total Amount to Pay: ₹" + totalAmount);


            int methodChoice = 0;
            System.out.println("\nChoose Payment Method:");
            System.out.println("1. UPI");
            System.out.println("2. Debit Card");
            System.out.println("3. Credit Card");
            System.out.println("4. Net Banking");

            while (true) {
                System.out.print("Enter option (1-4): ");
                if (sc.hasNextDouble()) {

                    methodChoice = sc.nextInt();
                    sc.nextLine();
                    break;

                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();

                }
            }


            String paymentMethod;
            switch (methodChoice) {
                case 1: paymentMethod = "UPI";
                    break;
                case 2: paymentMethod = "Debit Card";
                    break;
                case 3: paymentMethod = "Credit Card";
                    break;
                case 4: paymentMethod = "Net Banking";
                    break;
                default:
                    System.out.println("Invalid choice! Defaulting to UPI.");
                    paymentMethod = "UPI";
                    break;
            }
            String confirm = "";
            boolean flag = true;
            double userAmount = 0;
            while (flag)
            {
                System.out.print("Enter a Amount : ");
                if (sc.hasNextDouble()) {
                    userAmount = sc.nextDouble();
                    sc.nextLine();

                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();
                    continue;
                }


                if(userAmount == totalAmount)
                {
                    System.out.print("Confirm payment via " + paymentMethod + "? (yes/no): ");
                    confirm = sc.next();
                    flag = false;
                }
                else
                {
                    System.out.println("please enter valid amount!!");
                    continue;
                }

                if (confirm.equalsIgnoreCase("yes")) {


                    String updateQuery = "UPDATE bills SET status = 'PAID' WHERE service_number = ? AND status = 'Unpaid'";
                    pstmt = con.prepareStatement(updateQuery);
                    pstmt.setString(1, serviceNumber);
                    int updatedRows = pstmt.executeUpdate();

                    System.out.println("Payment successful! " + updatedRows + " bill(s) marked as Paid using " + paymentMethod + ".");


                    String typeQuery = "SELECT connection_type FROM customers WHERE service_number = ?";
                    PreparedStatement pst1 = con.prepareStatement(typeQuery);
                    pst1.setString(1, serviceNumber);
                    ResultSet rs1 = pst1.executeQuery();
                    String connectionType = "";
                    if (rs1.next()) {
                        connectionType = rs1.getString("connection_type");
                    }

                    rs1.close();
                    pst1.close();


                    String insertHistory = "INSERT INTO bill_history (service_number, billing_date,total_units, amount, connection_type) VALUES (?, CURDATE(), ?, ?,?)";
                    PreparedStatement insertHistoryStmt = con.prepareStatement(insertHistory);


                    for (int i = 0; i < billIds.size(); i++) {

                        insertHistoryStmt.setString(1, serviceNumber);
                        insertHistoryStmt.setDouble(2, units.get(i));
                        insertHistoryStmt.setDouble(3, amounts.get(i));
                        insertHistoryStmt.setString(4, connectionType);
                        insertHistoryStmt.executeUpdate();


                        CallableStatement cs = con.prepareCall("{CALL AddPayment(?, ?, ?)}");
                        cs.setInt(1, billIds.get(i));
                        cs.setString(2, paymentMethod);
                        cs.setDouble(3, amounts.get(i));
                        cs.execute();

                        cs.close();
                    }


                    break;

                } else if (confirm.equalsIgnoreCase("No")) {
                    System.out.println("Payment cancelled.");
                    break;

                } else {
                    System.out.println("Invalid Input!!");
                    flag = true;
                }

            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    static void viewBillHistoryConsumer(String serviceNumber) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT * FROM bill_history WHERE service_number = ? ORDER BY billing_date DESC";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, serviceNumber);
            rs = pstmt.executeQuery();

            System.out.println("\n========== Bill Payment History ==========");

            boolean hasHistory = false;
            while (rs.next()) {
                hasHistory = true;
                System.out.println("------------------------------------------");
                System.out.println("Date           : " + rs.getString("billing_date"));
                System.out.println("Units Consumed  : " + rs.getInt("total_units"));
                System.out.println("Amount Paid     : ₹" + rs.getDouble("amount"));
            }

            if (!hasHistory) {
                System.out.println("No bill payment history found for Service Number: " + serviceNumber);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    static void registerComplaint(String serviceNumber, String complaintText) {
        PreparedStatement pstmt = null;

        try {
            String sql = "INSERT INTO complaints(service_number, complaint_text, status, complaint_date) VALUES (?, ?, 'Pending', CURRENT_DATE)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, serviceNumber);
            pstmt.setString(2, complaintText);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Complaint registered successfully.");
            } else {
                System.out.println("Failed to register complaint.");
            }

        } catch (Exception e) {
            System.out.println("Error registering complaint: " + e.getMessage());
        }
    }


    static void viewComplaintStatus(String serviceNumber) {
        try {
            connection();
            String query = "SELECT * FROM complaints WHERE service_number = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, serviceNumber);
            ResultSet rs = pst.executeQuery();
            boolean found = false;

            System.out.println("\n--- Complaint Status ---");
            while (rs.next()) {
                found = true;
                System.out.println("Complaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Description: " + rs.getString("complaint_text"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("-----------------------------");
            }
            if (!found) {
                System.out.println("No complaints found for this service number.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void downloadBillReceipt(String serviceNumber) {



        try {

            String query = "SELECT p.payment_id, p.payment_date, p.payment_method, p.amount_paid, " +
                    "b.billing_date, b.total_units, b.amount, b.service_number " +
                    "FROM payments p " +
                    "INNER JOIN bills b ON p.bill_id = b.bill_id " +
                    "WHERE b.service_number = ? " +
                    "ORDER BY p.payment_date DESC LIMIT 1";


            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, serviceNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fileName = "Bill_Receipt_ServiceNo_" + serviceNumber + ".txt";
                FileWriter writer = new FileWriter(fileName,true);


                String line1 = "====================================";
                String line2 = "        ELECTRICITY BILL RECEIPT    ";
                String line3 = "====================================";
                String line4 = "Service Number : " + rs.getString("service_number");
                String line5 = "Bill Date      : " + rs.getDate("billing_date");
                String line6 = "Units Consumed : " + rs.getDouble("total_units");
                String line7 = "Bill Amount    : ₹" + rs.getDouble("amount");
                String line8 = "Payment Date   : " + rs.getDate("payment_date");
                String line9 = "Payment Amount : ₹" + rs.getDouble("amount_paid");
                String line10 = "Payment Method : " + rs.getString("payment_method");
                String line11 = "   Thank you for your payment!      ";


                writer.write(line1 + "\n");
                writer.write(line2 + "\n");
                writer.write(line3 + "\n");
                writer.write(line4 + "\n");
                writer.write(line5 + "\n");
                writer.write(line6 + "\n");
                writer.write(line7 + "\n");
                writer.write(line8 + "\n");
                writer.write(line9 + "\n");
                writer.write(line10 + "\n");
                writer.write(line11 + "\n");

                writer.close();


                System.out.println(line1);
                System.out.println(line2);
                System.out.println(line3);
                System.out.println(line4);
                System.out.println(line5);
                System.out.println(line6);
                System.out.println(line7);
                System.out.println(line8);
                System.out.println(line9);
                System.out.println(line10);
                System.out.println(line11);


            } else {
                System.out.println("No payment record found for Service Number: " + serviceNumber);
                System.out.println("Ensure a payment has been made for this service.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    static void receiveFeedback(String serviceNumber) {
        Scanner sc = new Scanner(System.in);
        PreparedStatement pstmt = null;

        try {
            String deleteQuery = "DELETE FROM feedback WHERE service_number = ?";
            pstmt = con.prepareStatement(deleteQuery);
            pstmt.setString(1, serviceNumber);
            pstmt.executeUpdate();

            int rating = 0;

            System.out.println("\n--- Feedback Section ---");
            while (true) {
                System.out.print("Rate your experience (1 to 5): ");
                if (sc.hasNextInt()) {

                    rating = sc.nextInt();
                    sc.nextLine();
                    break;

                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();

                }
            }



            while (rating < 1 || rating > 5) {
                System.out.print("Invalid rating. Please enter a number between 1 and 5: ");
                rating = sc.nextInt();

            }

            System.out.print("Any additional comments (optional): ");
            String comments = sc.next();
            String co = comments.isEmpty() ? null : comments;

            String insertQuery = "INSERT INTO feedback (service_number, rating, comments, feedback_date) VALUES (?, ?, ?, CURRENT_DATE)";
            pstmt = con.prepareStatement(insertQuery);
            pstmt.setString(1, serviceNumber);
            pstmt.setInt(2, rating);
            pstmt.setString(3, co);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Thank you for your feedback!");
            } else {
                System.out.println("Failed to record your feedback.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    static void advancePayment(String serviceNumber) {
        Scanner sc = new Scanner(System.in);
        System.out.println();
        System.out.println("--- Special Advance Payment Offers ---");
        System.out.println("1. Offer A: Pay ₹1000 → 5% discount on NEXT 2 bills");
        System.out.println("2. Offer B: Pay ₹5,000 → 8% discount on NEXT 5 bills");
        System.out.println("3. Offer C: Pay ₹20,000 → Lifetime 10% discount on ALL bills");

        while (true)
        {
            System.out.print("If you are interested in our special offers (yes/no): ");
            String choiceOffer = sc.next();
            if (choiceOffer.equalsIgnoreCase("yes")) {
                int count = 0;
                try {
                    String select = "SELECT COUNT(*) FROM advance_payments WHERE service_number = ?";
                    PreparedStatement pstmt = con.prepareStatement(select);
                    pstmt.setString(1, serviceNumber);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        count = rs.getInt(1);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                if (count > 0) {
                    System.out.println("You have already activated an advance payment offer!");
                    return;
                }


                int totalAmount = 0;
                char offerChar = 'D';

                int offer = 0;
                while (true) {

                    System.out.print("Choose an offer (1-3): ");
                    if (sc.hasNextInt()) {

                        offer = sc.nextInt();
                        sc.nextLine();

                    } else {
                        System.out.println("Invalid input! Please enter a number.");
                        sc.nextLine();
                        continue;
                    }


                    if (offer == 1) {
                        totalAmount = 1000;
                        offerChar = 'A';
                        break;
                    } else if (offer == 2) {
                        totalAmount = 5000;
                        offerChar = 'B';
                        break;
                    } else if (offer == 3) {
                        totalAmount = 20000;
                        offerChar = 'C';
                        break;
                    } else {
                        System.out.println("Enter a valid choice!!");
                    }
                }

                int methodChoice = 0;
                System.out.println("\nChoose Payment Method:");
                System.out.println("1. UPI");
                System.out.println("2. Debit Card");
                System.out.println("3. Credit Card");
                System.out.println("4. Net Banking");
                while (true) {
                    System.out.print("Enter option (1-4): ");
                    if (sc.hasNextDouble()) {

                        methodChoice = sc.nextInt();
                        sc.nextLine();
                        break;

                    } else {
                        System.out.println("Invalid input! Please enter a number.");
                        sc.nextLine();
                        continue;
                    }
                }

                String paymentMethod;
                switch (methodChoice) {
                    case 1:
                        paymentMethod = "UPI";
                        break;
                    case 2:
                        paymentMethod = "Debit Card";
                        break;
                    case 3:
                        paymentMethod = "Credit Card";
                        break;
                    case 4:
                        paymentMethod = "Net Banking";
                        break;
                    default:
                        System.out.println("Invalid choice! Defaulting to UPI.");
                        paymentMethod = "UPI";
                        break;
                }
                String confirm = "";
                boolean flag = true;
                while (flag) {
                    System.out.print("Enter a Amount : ");
                    double userAmount = sc.nextDouble();

                    if (userAmount == totalAmount) {
                        System.out.print("Confirm payment via " + paymentMethod + "? (yes/no): ");
                        confirm = sc.next();
                        flag = false;
                    } else {
                        System.out.println("please enter valid amount!!");
                    }
                }
                System.out.println("Advance payment successful!");
                System.out.println("Congratulations! Offer : " + offerChar + " activated ");
                AdvanceBillPayment obj = new AdvanceBillPayment(con, serviceNumber);
                try {
                    obj.makePayment(serviceNumber, totalAmount, offerChar);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                double discount = obj.getDiscount(offerChar);
                System.out.println("Your Discount is: " + discount + "%");
                break;

            } else if (choiceOffer.equalsIgnoreCase("No")) {
                System.out.println("Thank you!!");
                break;
            } else {
                System.out.println("Not Valid choice");
            }

        }


    }



    public static boolean isValidDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate.parse(dateString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }



}


interface AdvancePayment {

    double getDiscount(double amount);
}

abstract class BillPayment implements  AdvancePayment{
    protected Connection con;
    protected String serviceNumber;

    public BillPayment(Connection con, String serviceNumber) {
        this.con = con;
        this.serviceNumber = serviceNumber;
    }


    public double getDiscount(double amount) {
        return 0.0;
    }
}

class AdvanceBillPayment extends BillPayment {


    public AdvanceBillPayment(Connection con, String serviceNumber) {
        super(con, serviceNumber);
    }


    public void makePayment(String serviceNumber, double amount,  char type) throws Exception {
        String query = "INSERT INTO advance_payments(service_number, amount, payment_date, discount_bills_number,discount_number) VALUES (?, ?, CURDATE(), ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, serviceNumber);
        pstmt.setDouble(2, amount);
        int total_bills_discount = 0;
        String dicount_number = "";
        if(type == 'A')
        {
            total_bills_discount = 2;
            dicount_number = "5";
        } else if (type == 'B') {
            total_bills_discount = 5;
            dicount_number = "8";
        }
        else
        {
            total_bills_discount = 12;
            dicount_number = "10";
        }
        pstmt.setInt(3, total_bills_discount);
        pstmt.setString(4,dicount_number);
        pstmt.executeUpdate();


    }


    public double getDiscount(char type) {
        if(type == 'A')
        {
            return 5;
        } else if (type == 'B') {
            return 8;
        }
        else
        {
            return 10;
        }
    }
}
