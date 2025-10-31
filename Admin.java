import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.io.*;

public class Admin {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    static Connection con;

    public Admin() throws Exception{
        connection();
        login();

    }
    static void connection() throws Exception {
        // Class.forName("cj.");
        


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

    static void login() throws Exception{
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.println();
            System.out.println("------ Admin Login ------");
            System.out.print("Enter Admin Username: ");
            String username = sc.nextLine();

            System.out.print("Enter Admin Password: ");
            String password = sc.nextLine();


            if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                System.out.println("Login successful! Welcome Admin.");
                menuDisplay();
                break;
            } else {
                System.out.println("Invalid credentials! Access denied.");
            }
        }
    }

    static void menuDisplay() throws Exception {
        Scanner sc = new Scanner(System.in);
        int choice;
        boolean flag = false;
        while (!flag) {
            System.out.println("\n******** Admin Panel ********");
            System.out.println("1. Search Consumer Details");
            System.out.println("2. Upload Meter Readings");
            System.out.println("3. Generate Monthly Bills");
            System.out.println("4. View Bill History of Any Consumer");
            System.out.println("5. View & Resolve Complaints");
            System.out.println("6. View Feedback");
            System.out.println("7. Generate Reports");
            System.out.println("8. Upload Meter Data");
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
                    searchCustomer();
                    break;
                case 2:
                    uploadMeterReadings();
                    break;
                case 3:
                    generateMonthlyBills();
                    break;
                case 4:
                    viewBillHistory();
                    break;
                case 5:
                    resolveComplaints();
                    break;
                case 6:
                    viewFeedback();
                    break;
                case 7:
                    generateReports();
                    break;
                case 8:
                    addMeter();
                    break;
                case 9:
                    flag = true;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice! Please select again.");
                    break;
            }
        }
    }

    static void searchCustomer() throws Exception {
        String query = "SELECT * FROM customers";
        PreparedStatement pstmt = con.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        ConsumerBST bst = new ConsumerBST();
        int count = 0;

        while (rs.next()) {
            String serviceNumber = rs.getString("service_number");
            String name = rs.getString("name");
            String address = rs.getString("address");
            String contactNumber = rs.getString("contact_number");
            String email = rs.getString("email");
            String connectionType = rs.getString("connection_type");
            String connectionDate = rs.getString("connection_date");
            String password = rs.getString("password");

            Consumer consumer = new Consumer(serviceNumber, name, address, contactNumber,
                    email, connectionType, connectionDate, password);
            bst.insert(consumer);
            count++;
        }

        if (count == 0) {
            System.out.println("No consumer records found.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Service Number to search: ");
        String searchNumber = sc.nextLine();

        Consumer result = bst.searchByServiceNumber(searchNumber);
        if (result != null) {
            System.out.println("\nCustomer found:");
            result.display();
        } else {
            System.out.println("Service Number " + searchNumber + " not found.");
        }
    }



    static void uploadMeterReadings() throws Exception {
        Scanner sc = new Scanner(System.in);

        try {
            int meterId = 0;
            while (true) {
                System.out.println("=== Upload Meter Reading ===");
                System.out.print("Enter Meter ID: ");
                if (sc.hasNextInt()) {

                    meterId = sc.nextInt();
                    sc.nextLine();

                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();
                    continue;
                }


                String slect = "SELECT COUNT(*) FROM meters WHERE meter_id = ?";
                PreparedStatement pstmt1 = con.prepareStatement(slect);
                pstmt1.setInt(1, meterId);
                ResultSet rs1 = pstmt1.executeQuery();
                int count = 0;
                if (rs1.next()) {
                    count = rs1.getInt(1);
                    if (count == 0) {
                        System.out.println("Not Present This Type Of Meter!!");
                    } else {
                        break;

                    }
                }
            }


            String preReadind = "SELECT current_reading FROM readings WHERE meter_id = ? ORDER BY reading_date DESC LIMIT 1";
            PreparedStatement fetchStmt = con.prepareStatement(preReadind);
            fetchStmt.setInt(1, meterId);
            ResultSet rs = fetchStmt.executeQuery();

            double previous = 0;
            if (rs.next()) {
                previous = rs.getDouble("current_reading");
                System.out.println("Previous Reading (auto-fetched): " + previous);
            } else {
                System.out.println("No previous reading found. This will be the first entry.");
            }


            double current;
            while (true) {
                System.out.print("Enter Current Reading: ");
                if (sc.hasNextDouble()) {

                    current = sc.nextDouble();


                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();
                    continue;
                }

                if (current > previous) {
                    break;
                } else {
                    System.out.println("Invalid! Current reading must be greater than previous reading (" + previous + ").");
                }
            }

            sc.nextLine();


            String readingDate = "";
            while (true) {
                System.out.print("Enter Reading Date (YYYY-MM-DD): ");
                readingDate = sc.nextLine();

                if (isValidDate(readingDate)) {
                    break;
                } else {
                    System.out.println("Invalid Date!!!");
                }
            }


            String insertSQL = "INSERT INTO readings (meter_id, reading_date, current_reading, previous_reading) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(insertSQL);
            pstmt.setInt(1, meterId);
            pstmt.setDate(2, Date.valueOf(readingDate));
            pstmt.setDouble(3, current);
            pstmt.setDouble(4, previous);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Meter reading uploaded successfully.");
            } else {
                System.out.println("Failed to upload meter reading.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    static double calculateBill(double units, String connectionType) {
        double amount = 0;

        switch (connectionType) {
            case "Commercial":
                if (units <= 5) {
                    amount = units * 20;
                } else if (units <= 15) {
                    amount = 5 * 20 + (units - 5) * 25;
                } else {
                    amount = 5 * 20 + 10 * 25 + (units - 15) * 30;
                }
                break;

            case "Industrial":
                if (units <= 5) {
                    amount = units * 25;
                } else if (units <= 15) {
                    amount = 5 * 25 + (units - 5) * 30;
                } else {
                    amount = 5 * 25 + 10 * 30 + (units - 15) * 35;
                }
                break;

            default: // Domestic
                if (units <= 5) {
                    amount = units * 12;
                } else if (units <= 15) {
                    amount = 5 * 12 + (units - 5) * 15;
                } else {
                    amount = 5 * 12 + 10 * 15 + (units - 15) * 20;
                }
        }

        return amount;
    }


    static void generateMonthlyBills() throws SQLException {
        try {
            String query =
                    "SELECT  meters.service_number," +
                            "    readings.units_consumed," +
                            "    readings.reading_date," +
                            "    customers.connection_type" +
                            " FROM readings" +
                            " JOIN meters ON readings.meter_id = meters.meter_id" +
                            " JOIN customers ON meters.service_number = customers.service_number" +
                            " WHERE readings.reading_date = (" +
                            "    SELECT MAX(r2.reading_date)" +
                            "    FROM readings r2" +
                            "    WHERE r2.meter_id = readings.meter_id" +
                            ")";

            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();


            String insertBill =
                    "INSERT INTO bills (service_number, billing_year, billing_month, billing_date, due_date, total_units, amount) " +
                            "VALUES (?, YEAR(?), MONTH(?), ?, DATE_ADD(?, INTERVAL 15 DAY), ?, ?)";

            PreparedStatement billStmt = con.prepareStatement(insertBill);

            int countGenerated = 0;
            int countSkipped = 0;

            while (rs.next()) {
                String serviceNumber = rs.getString("service_number");
                double units = rs.getDouble("units_consumed");
                Date readingDate = rs.getDate("reading_date");
                String connType = rs.getString("connection_type");


                String checkQuery = "SELECT COUNT(*) FROM bills WHERE service_number = ? AND billing_date = ?";
                PreparedStatement checkStmt = con.prepareStatement(checkQuery);
                checkStmt.setString(1, serviceNumber);
                checkStmt.setDate(2, readingDate);
                ResultSet checkRs = checkStmt.executeQuery();
                checkRs.next();
                int existingCount = checkRs.getInt(1);

                checkRs.close();
                checkStmt.close();

                if (existingCount > 0) {
                    countSkipped++;
                    continue;
                }


                double amount = calculateBill(units, connType);


                billStmt.setString(1, serviceNumber);
                billStmt.setDate(2, readingDate);
                billStmt.setDate(3, readingDate);
                billStmt.setDate(4, readingDate);
                billStmt.setDate(5, readingDate);
                billStmt.setDouble(6, units);
                billStmt.setDouble(7, amount);

                billStmt.addBatch();
                countGenerated++;
            }

            if (countGenerated > 0) {
                billStmt.executeBatch();
            }

            if (countGenerated == 0 && countSkipped > 0) {
                System.out.println("All bills are already generated for this month.");
            } else {
                System.out.println(countGenerated + " monthly bills generated successfully.");
                if (countSkipped > 0) {
                    System.out.println(countSkipped + " bills were already generated and skipped.");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }




    static void viewBillHistory() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Service Number to view bill history: ");
        String serviceNumber = sc.nextLine();


        String countQuery = "SELECT COUNT(*) AS total FROM bill_history WHERE service_number = ?";
        PreparedStatement countStmt = con.prepareStatement(countQuery);
        countStmt.setString(1, serviceNumber);
        ResultSet countRs = countStmt.executeQuery();

        int count = 0;
        if (countRs.next()) {
            count = countRs.getInt("total");
        }

        if (count == 0) {
            System.out.println("No bill history found for Service Number: " + serviceNumber);
            return;
        }


        String sql = "SELECT * FROM bill_history WHERE service_number = ? ORDER BY billing_date DESC";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, serviceNumber);
        ResultSet rs = pstmt.executeQuery();


        Bills billStack = new Bills(count);

        while (rs.next()) {
            String bill = "Bill ID : " + rs.getInt("bill_id") +
                    ", Service Number : " + rs.getString("service_number") +
                    ", Prev Reading : " + rs.getDate("billing_date") +
                    ", Units : " + rs.getDouble("total_units") +
                    ", Amount : ₹" + rs.getDouble("amount") +
                    ", Status : " + rs.getString("status") +
                    ", Connection : " + rs.getString("connection_type");
            billStack.push(bill);
        }

        System.out.println("\n--- Bill History for Service Number: " + serviceNumber + " (Latest First) ---");
        System.out.println();
        billStack.display();


        rs.close();
        pstmt.close();
        countRs.close();
        countStmt.close();
    }


    static void resolveComplaints() throws Exception {
        Scanner sc = new Scanner(System.in);


        String countQuery = "SELECT COUNT(*) AS total FROM complaints WHERE status = 'Pending'";
        PreparedStatement countStmt = con.prepareStatement(countQuery);
        ResultSet countRs = countStmt.executeQuery();

        int total = 0;
        if (countRs.next()) {
            total = countRs.getInt("total");
        }

        if (total == 0) {
            System.out.println("No pending complaints.");
            return;
        }
        int choice = 0;
        System.out.println("Total Number of complaints : "+total);
        boolean flag = true;
        while (flag) {

            System.out.print("Enter number to resolve this time ");
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();

            } else {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine();
                continue;
            }


            if (choice <= total) {
                flag = false;
            }
            else
            {
                System.out.println("please enter valid number!!");
            }
        }

        Complaints complaintQueue = new Complaints(total);
        String fetchQuery = "SELECT complaint_id, service_number, complaint_text FROM complaints WHERE status = 'Pending' ORDER BY complaint_id ASC LIMIT ?";
        PreparedStatement stmt = con.prepareStatement(fetchQuery);
        stmt.setInt(1,choice);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int compId = rs.getInt("complaint_id");
            String custId = rs.getString("service_number");
            String text = rs.getString("complaint_text");

            String fullComplaint = "Complaint ID: " + compId +
                    ", Service Number: " + custId +
                    ", Text: " + text;
            complaintQueue.enqueue(fullComplaint);
        }


        while (complaintQueue.size != 0) {
            String currentComplaint = complaintQueue.dequeue();
            System.out.println("\n" + currentComplaint);
            while (true) {
                System.out.print("Mark this complaint as resolved? (yes/no): ");
                String response = sc.next().trim().toLowerCase();

                if (response.equalsIgnoreCase("yes")) {

                    int idStart = currentComplaint.indexOf("Complaint ID: ") + 14;
                    int idEnd = currentComplaint.indexOf(",", idStart);
                    int complaintId = Integer.parseInt(currentComplaint.substring(idStart, idEnd));

                    String updateQuery = "UPDATE complaints SET status = 'Resolved' WHERE complaint_id = ?";
                    PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                    updateStmt.setInt(1, complaintId);
                    updateStmt.executeUpdate();

                    System.out.println("Complaint ID " + complaintId + " marked as resolved.");
                    break;
                } else if (response.equalsIgnoreCase("No")) {
                    System.out.println("Complaint skipped.");
                    break;
                } else {
                    System.out.println("Invalid Input!!");
                }
            }
        }


        rs.close();
        stmt.close();
        countRs.close();
        countStmt.close();
    }


    static void viewFeedback() {
        try {
            String query = "SELECT id, service_number, rating, comments, feedback_date FROM feedback";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== All Customer Feedback ==========");
            boolean hasFeedback = false;

            while (rs.next()) {
                hasFeedback = true;
                System.out.println("-------------------------------------------");
                System.out.println("Feedback ID     : " + rs.getInt("id"));
                System.out.println("Service Number  : " + rs.getString("service_number"));
                System.out.println("Rating          : " + rs.getInt("rating"));
                System.out.println("Comments        : " + rs.getString("comments"));
                System.out.println("Submitted On    : " + rs.getDate("feedback_date"));
            }

            if (!hasFeedback) {
                System.out.println("No feedback found.");
            }

            System.out.println("===========================================\n");
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    static void generateReports() throws Exception {
        Scanner sc = new Scanner(System.in);
        String timeFilter = "";
        String reportLabel = "";
        boolean flag = true;
        while (flag) {
            System.out.println("Select report type:");
            System.out.println("1. Daily Report");
            System.out.println("2. Monthly Report");
            System.out.println("3. Yearly Report");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();



            switch (choice) {
                case 1:
                    int dYear = getIntInput(sc, "Enter year (YYYY): ");
                    int dMonth = 0;
                    int dDay = 0;
                    while (true) {
                        dMonth = getIntInput(sc, "Enter month (1-12): ");
                        if(dMonth > 0 && dMonth <13)
                        {
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid Input!!");
                        }
                    }
                    while (true) {
                        dDay = getIntInput(sc, "Enter day (1-31): ");
                        if(dDay > 0 && dDay <32)
                        {
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid Input!!");
                        }
                    }
                    sc.nextLine();
                    timeFilter = "billing_year = " + dYear + " AND billing_month = " + dMonth + " AND DAY(billing_date) = " + dDay;
                    reportLabel = "Daily (Year: " + dYear + ", Month: " + dMonth + ", Day: " + dDay + ")";
                    flag = false;
                    break;
                case 2:
                    int mYear = getIntInput(sc, "Enter year (YYYY): ");
                    int mMonth = 0;
                    while (true) {
                        mMonth = getIntInput(sc, "Enter month (1-12): ");
                        if(mMonth > 0 && mMonth <13)
                        {
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid Input!!");
                        }
                    }
                    sc.nextLine();
                    timeFilter = "billing_year = " + mYear + " AND billing_month = " + mMonth;
                    reportLabel = "Monthly (Year: " + mYear + ", Month: " + mMonth + ")";
                    flag = false;
                    break;
                case 3:
                    int yYear = getIntInput(sc, "Enter year (YYYY): ");
                    sc.nextLine();
                    timeFilter = "billing_year = " + yYear;
                    reportLabel = "Yearly (Year: " + yYear + ")";
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid choice.");

            }

        }


        String query = "SELECT COUNT(*) AS total_bills, SUM(amount) AS total_amount FROM bills WHERE " + timeFilter;
        PreparedStatement pstmt = con.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        int totalBills = 0;
        double totalAmount = 0.0;

        if (rs.next()) {
            totalBills = rs.getInt("total_bills");
            totalAmount = rs.getDouble("total_amount");
        }

        if (totalBills == 0) {
            System.out.println("No bills found for the selected period.");
            return;
        }


        FileWriter fw = new FileWriter("report.txt",true);

        System.out.println("ELECTRICITY BILL MANAGEMENT REPORT");
        System.out.println("----------------------------------");
        System.out.println("Report Type: " + reportLabel);
        System.out.println("Total Bills Generated: " + totalBills);
        System.out.println("Total Revenue Collected: ₹" + totalAmount);
        System.out.println("----------------------------------");


        fw.write("ELECTRICITY BILL MANAGEMENT REPORT\n");
        fw.write("----------------------------------\n");
        fw.write("Report Type: " + reportLabel + "\n");
        fw.write("Total Bills Generated: " + totalBills + "\n");
        fw.write("Total Revenue Collected: ₹" + totalAmount + "\n");
        fw.write("----------------------------------\n");

        fw.close();
        System.out.println("Report also saved as report.txt");
    }


    static void addMeter() {
        Scanner sc = new Scanner(System.in);
        try {

            String[] serviceNumberDemo = new String[20];
            int countx = 0;


            String query1 = "SELECT service_number FROM customers  WHERE service_number NOT IN (SELECT service_number FROM meters)";


            PreparedStatement pst1 = con.prepareStatement(query1);

            ResultSet rs1 = pst1.executeQuery();
            while (rs1.next())
            {
                serviceNumberDemo[countx] = rs1.getString("service_number");
                System.out.print("This Service Number is for New Customer -- ");
                System.out.println(serviceNumberDemo[countx]);
                countx++;
            }

            if(countx == 0)
            {
                System.out.println("No New Customer join!!");
                return;
            }

            String serviceNumber = "";
            boolean flag = true;
            while (flag) {
                System.out.print("Enter a Service Number : ");
                serviceNumber = sc.next();
                for (String s : serviceNumberDemo) {
                    if (serviceNumber.equalsIgnoreCase(s)) {
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    System.out.println("Invalid Number!!");
                }

            }


            int meterId = 0;
            while (true) {
                System.out.print("Enter Meter ID : ");
                if (sc.hasNextInt()) {
                    meterId = sc.nextInt();
                    sc.nextLine();

                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.nextLine();
                    continue;
                }


                String query2 = "SELECT COUNT(*) FROM meters WHERE meter_id = ?";


                PreparedStatement pst = con.prepareStatement(query2);
                pst.setInt(1, meterId);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        System.out.println("Meter ID Already exists in the database.");
                    }
                    else
                    {
                        break;
                    }
                }

            }

            String meterType ="";
            while (true) {
                System.out.print("Enter Meter Type (Single Phase / Three Phase): ");
                meterType = sc.nextLine().trim();

                if(meterType.equalsIgnoreCase("Single Phase") || meterType.equalsIgnoreCase("Three Phase") )
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid Input!!");
                }
            }

            String dateInput = "";
            while (true) {
                System.out.print("Enter Installation Date (YYYY-MM-DD): ");
                dateInput = sc.nextLine().trim();

                if (isValidDate(dateInput)) {
                    break;
                }
                else
                {
                    System.out.println("Invalid Date!!!");
                }
            }


            String query = "INSERT INTO meters (meter_id, service_number, meter_type, installation_date) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, meterId);
            pstmt.setString(2, serviceNumber);
            pstmt.setString(3, meterType);
            pstmt.setString(4, dateInput);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Meter details added successfully!");
            } else {
                System.out.println("Failed to add meter details.");
            }

        } catch (Exception e) {
            System.out.println("This service number not exists");
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

    public static int getIntInput(Scanner sc, String prompt) {
        int value;
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                value = sc.nextInt();
                break;
            } else {
                System.out.println("Invalid input! Please enter an integer.");
                sc.next();
            }
        }
        return value;
    }


}

class Bills {
    int top;
    int capacity;
    String[] stackArray;

    Bills(int size) {
        capacity = size;
        top = -1;
        stackArray = new String[capacity];
    }

    void push(String data) {
        if (top == capacity - 1) {
            System.out.println("Stack Overflow!");
            return;
        }
        stackArray[++top] = data;
    }

    void display() {
        if (top == -1) {
            System.out.println("Stack is empty.");
            return;
        }
        for (int i = top; i >= 0; i--) {
            String a = stackArray[i];
            String[] parts = a.split(",");
            for (String part : parts) {
                System.out.println(part.trim());
            }
            System.out.println("-----------------------------------------------------------");
        }
    }

}
class Complaints {
    int front, rear, size, capacity;
    String[] queueArray;

    Complaints(int capacity) {
        this.capacity = capacity;
        front = 0;
        rear = -1;
        size = 0;
        queueArray = new String[capacity];
    }

    void enqueue(String complaint) {
        if (size == capacity) {
            System.out.println("Complaint queue is full!");
            return;
        }
        rear = (rear + 1) % capacity;
        queueArray[rear] = complaint;
        size++;
    }

    String dequeue() {
        if (size == 0) {
            return null;
        }
        String complaint = queueArray[front];
        front = (front + 1) % capacity;
        size--;
        return complaint;
    }

    public void display() {
        if (size == 0) {
            System.out.println("No pending complaints.");
            return;
        }

        System.out.println("Pending Complaints:");
        for (int i = 0; i < size; i++) {
            int index = (front + i) % capacity;
            System.out.println((i + 1) + ". " + queueArray[index]);
        }
    }
}


class Consumer {
    String serviceNumber;
    String name;
    String address;
    String contactNumber;
    String email;
    String connectionType;
    String connectionDate;
    String password;

    // Constructor
    public Consumer(String serviceNumber, String name, String address, String contactNumber,
                    String email, String connectionType, String connectionDate, String password) {
        this.serviceNumber = serviceNumber;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.connectionType = connectionType;
        this.connectionDate = connectionDate;
        this.password = password;
    }

    // Display method
    public void display() {
        System.out.println("---------------------------");
        System.out.println("Service Number : " + serviceNumber);
        System.out.println("Name           : " + name);
        System.out.println("Address        : " + address);
        System.out.println("Contact Number : " + contactNumber);
        System.out.println("Email          : " + email);
        System.out.println("Connection Type: " + connectionType);
        System.out.println("Connection Date: " + connectionDate);
        System.out.println("Password       : " + password);
    }
}




// It is use for store details of customer
class ConsumerBST {

    class Node {
        Consumer data;
        Node left, right;

        public Node(Consumer data) {
            this.data = data;
            this.left = this.right = null;
        }
    }

    Node root;


    public void insert(Consumer data) {
        root = insertRec(root, data);
    }

    private Node insertRec(Node root, Consumer data) {
        if (root == null) {
            return new Node(data);
        }
        if (data.serviceNumber.compareTo(root.data.serviceNumber) < 0) {
            root.left = insertRec(root.left, data);
        } else {
            root.right = insertRec(root.right, data);
        }
        return root;
    }


    public Consumer searchByServiceNumber(String serviceNumber) {
        return searchRec(root, serviceNumber);
    }

    private Consumer searchRec(Node root, String serviceNumber) {
        if (root == null) return null;

        int cmp = serviceNumber.compareTo(root.data.serviceNumber);
        if (cmp == 0) {
            return root.data;
        } else if (cmp < 0) {
            return searchRec(root.left, serviceNumber);
        } else {
            return searchRec(root.right, serviceNumber);
        }
    }


}
