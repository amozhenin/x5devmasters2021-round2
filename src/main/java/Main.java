import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        int n = scan.nextInt();
        int l = scan.nextInt();

        int div = l / n;
        int rem = l % n;
        if (rem == 0) {
            System.out.println(div);
        } else {
            System.out.println(div + 1);
        }
    }
}
