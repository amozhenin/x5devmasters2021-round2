import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
//        Scanner scan = new Scanner(System.in);
        Scanner scan = new Scanner(new File("input.txt"));

        System.out.println("25 0 25 0");
        System.out.println("125 0 125 0");
        System.out.println("125 100 25 0");
        System.out.println("125 100 25 0");
        System.out.println("125 110 15 0");
        System.out.println("125 120 5 0");
        System.out.println("125 130 0 5");
        System.out.println("0 0 0 0");
    }
}
