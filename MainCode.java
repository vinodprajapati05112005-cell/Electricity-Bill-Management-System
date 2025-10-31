import java.util.Scanner;

public class MainCode {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== Welcome to the System =====");
        System.out.println("Please select 1your role:");
        System.out.println("1. Admin");
        System.out.println("2. Customer");
        int choice = 0;
        while (true) {
            try {
                System.out.print("Enter your choice: ");

                if (sc.hasNextInt()) {
                    choice = sc.nextInt();

                    if (choice == 1) {
                        new Admin();
                        break;
                    } else if (choice == 2) {
                        new Customer();
                        break;
                    } else {
                        System.out.println("Not valid Choice!!");
                    }

                } else {

                    System.out.println("Invalid Choice!!");
                    sc.next();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            }
        }


    }
}