import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        int pallets[] = new int[n];
        for (int i = 0; i < n; i++) {
            pallets[i] = scan.nextInt();
        }
        Arrays.sort(pallets);
        int pairs = n / 2;
        int sum = 0;
        for (int i = 0; i < pairs; i++) {
            sum += pallets[2 * i + 1] - pallets[2 * i];
        }
        System.out.println(sum);
    }
}
