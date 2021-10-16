import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
//        Scanner scan = new Scanner(new File("input.txt"));

        int productsCount = scan.nextInt();
        int moneyCount = scan.nextInt();
        int opsCount = scan.nextInt();

        Product[] products = new Product[productsCount + 1]; //zero index is empty

        for (int i = 0; i < productsCount; i++) {
            int id = scan.nextInt();
            int qty = scan.nextInt();
            int price = scan.nextInt();
            Product product = new Product(id, qty, price);
            products[id] = product;
        }

        Map<Integer, Money> money = new HashMap<>();

        for (int i = 0; i < moneyCount; i++) {
            int nominal = scan.nextInt();
            int count = scan.nextInt();
            Money coinBlock = new Money(nominal, count);
            money.put(nominal, coinBlock);
        }

        scan.nextLine();
        Operation[] operations = new Operation[opsCount];
        for (int i = 0; i < opsCount; i++) {
            String opLine = scan.nextLine();
            Operation op = parseOperation(opLine);
            operations[i] = op;
        }

        Basket basket = initBasket(products, money);
        Status status = new Status(0, 0, 0, 0, basket, products, money);
        for (int i = 0; i < opsCount; i++) {
            status = updateStatus(status, operations[i]);
            System.out.println(status);
        }
    }

    private static Operation parseOperation(String line) {
        String[] args = line.split(" ");
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return new Operation(OpClass.UNKNOWN, null);
        }
        Integer argument = null;
        OpClass opClass = OpClass.findById(id);
        if (opClass == OpClass.BUY || opClass == OpClass.MONEY) {
            try {
                argument = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                argument = 0;
            }
        }
        Operation op = new Operation(opClass, argument);
        return op;
    }

    private static Basket initBasket(Product[] products, Map<Integer, Money> moneyMap) {
        Product[] basketProducts = new Product[products.length];
        for (int i = 1; i < basketProducts.length; i++) {
            Product product = products[i];
            basketProducts[i] = new Product(product.id, 0, product.price);
        }
        Map<Integer, Money> basketMap = new HashMap<>();
        for (Integer nominal : moneyMap.keySet()) {
            basketMap.put(nominal, new Money(nominal, 0));
        }
        Basket basket = new Basket(basketProducts, basketMap);
        return basket;
    }

    private static Status updateStatus(Status status, Operation op) {
        switch (op.getOpClass()) {
            case BUY:
                if (status.getProducts()[op.getArgument()].getQuantity() - status.getBasket().getProducts()[op.getArgument()].getQuantity() > 0) {
                    status.getBasket().addProduct(op.getArgument());
                }
                break;
            case MONEY:
                if (status.getMoneyMap().get(op.getArgument()) != null) {
                    status.getBasket().addMoney(op.getArgument());
                }
                break;
            case CHECKOUT:
                Money[] changeList = tryChange(status.getMoneyMap(), status.getChange());
                if (status.getRemaining() == 0 && changeList != null) {
                    doCheckout(status, changeList);
                }
                break;
            case CANCEL:
                status.getBasket().clear();
                break;
        }
        recalculateStatus(status);
        return status;
    }

    public static Money[] tryChange(Map<Integer, Money> moneyMap, long change) {
        long input = getInput(moneyMap);
        if (input < change) {
            return null;
        }
        Money[] money = new Money[moneyMap.values().size()];
        moneyMap.values().toArray(money);
        Arrays.sort(money, (o1, o2) -> o2.getNominal() - o1.getNominal());
        Money[] changeList = new Money[money.length];
        for (int i = 0; i < changeList.length; i++) {
            changeList[i] = new Money(money[i].getNominal(), 0);
        }
        return recursiveChange(money, changeList, change);
    }

    private static Money[] recursiveChange(Money[] moneyList, Money[] changeList, long change) {
        if (change < 0) {
            return null;
        }
        if (change == 0) {
            return changeList;
        }
        Money[] ret = null;
        for (int i = 0; i < moneyList.length; i++) {
            if (moneyList[i].getNominal() > change) {
                continue;
            }
            if (moneyList[i].getCount() == 0) {
                continue;
            }
            Money[] moneyListCopy = new Money[moneyList.length];
            for (int j = 0; j < moneyListCopy.length; j++) {
                moneyListCopy[j] = new Money(moneyList[j].getNominal(), moneyList[j].getCount());
            }
            moneyListCopy[i].setCount(moneyListCopy[i].getCount() - 1);
            Money[] changeListCopy = new Money[changeList.length];
            for (int j = 0; j < changeListCopy.length; j++) {
                changeListCopy[j] = new Money(changeList[j].getNominal(), changeList[j].getCount());
            }
            changeListCopy[i].setCount(changeListCopy[i].getCount() + 1);
            ret = recursiveChange(moneyListCopy, changeListCopy, change - moneyList[i].getNominal());
            if (ret != null) {
                break;
            }
        }
        return ret;
    }

    public static void doChange(Status status, Money[] changeList) {
        for (Money changeMoney : changeList) {
            Money money = status.getMoneyMap().get(changeMoney.getNominal());
            money.setCount(money.getCount() - changeMoney.getCount());
        }
    }

    public static long getCost(Product[] products) {
        long ret = 0;
        for (int i = 1; i < products.length; i++) {
            ret += (long) products[i].getPrice() * (long) products[i].getQuantity();
        }
        return ret;
    }

    public static long getInput(Map<Integer, Money> moneyMap) {
        long ret = 0;
        for (Money money : moneyMap.values()) {
            ret += (long) money.getNominal() * (long) money.getCount();
        }
        return ret;
    }

    private static Status recalculateStatus(Status status) {
        long cost = getCost(status.getBasket().getProducts());
        long input = getInput(status.getBasket().getMoneyMap());
        status.setCost(cost);
        status.setInput(input);
        status.setRemaining(cost <= input ? 0 : cost - input);
        status.setChange(cost <= input ? input - cost : 0);
        return status;
    }

    private static void doCheckout(Status status, Money[] changeList) {
        Product[] products = status.getProducts();
        Product[] basketProducts = status.getBasket().getProducts();
        for (int i = 1; i < products.length; i++) {
            int newQty = products[i].getQuantity() - basketProducts[i].getQuantity();
            products[i].setQuantity(newQty);
        }
        doChange(status, changeList);
        Map<Integer, Money> moneyMap = status.getMoneyMap();
        Map<Integer, Money> basketMoney = status.getBasket().getMoneyMap();
        for (Integer nominal : moneyMap.keySet()) {
            Money money = moneyMap.get(nominal);
            int newCount = money.getCount() + basketMoney.get(nominal).getCount();
            money.setCount(newCount);
        }

        status.getBasket().clear();
    }

    private static class Product {
        private final int id;
        private int quantity;
        private final int price;

        public Product (int id, int quantity, int price) {
            this.id = id;
            this.quantity = quantity;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getPrice() {
            return price;
        }

        public void setQuantity(int qty) {
            this.quantity = qty;
        }
    }

    private static class Money {
        private final int nominal;
        private int count;

        public Money(int nominal, int count) {
            this.nominal = nominal;
            this.count = count;
        }

        public int getNominal() {
            return nominal;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    private static class Operation {
        private final OpClass opClass;
        private final Integer argument;

        public Operation(OpClass opClass, Integer argument) {
            this.opClass = opClass;
            this.argument = argument;
        }

        public OpClass getOpClass() {
            return opClass;
        }

        public Integer getArgument() {
            return argument;
        }
    }

    private enum OpClass {
        BUY(1),
        MONEY(2),
        CHECKOUT(3),
        CANCEL(4),
        UNKNOWN(-1);

        OpClass(int id) {
            this.id = id;
        }

        private int id;

        public int getId() {
            return id;
        }

        public static OpClass findById(int idToFind) {
            return Arrays.stream(values()).filter(op -> op.getId() == idToFind).findFirst().orElse(null);
        }
    }

    private static class Status {
        private long cost;
        private long input;
        private long remaining;
        private long change;

        private final Basket basket;
        private final Product[] products;
        private final Map<Integer, Money> moneyMap;

        public long getCost() {
            return cost;
        }

        public void setCost(long cost) {
            this.cost = cost;
        }

        public long getInput() {
            return input;
        }

        public void setInput(long input) {
            this.input = input;
        }

        public long getRemaining() {
            return remaining;
        }

        public void setRemaining(long remaining) {
            this.remaining = remaining;
        }

        public long getChange() {
            return change;
        }

        public void setChange(long change) {
            this.change = change;
        }

        public Basket getBasket() {
            return basket;
        }

        public Product[] getProducts() {
            return products;
        }

        public Map<Integer, Money> getMoneyMap() {
            return moneyMap;
        }

        public Status(int cost, int input, int remaining, int change, Basket basket, Product[] products, Map<Integer, Money> moneyMap) {
            this.cost = cost;
            this.input = input;
            this.remaining = remaining;
            this.change = change;
            this.basket = basket;
            this.products = products;
            this.moneyMap = moneyMap;
        }

        @Override
        public String toString() {
            return cost + " " + input + " " + remaining + " " + change;
        }
    }

    private static class Basket {
        private final Product[] products;
        private final Map<Integer, Money> moneyMap;

        public Basket(Product[] products, Map<Integer, Money> moneyMap) {
            this.products = products;
            this.moneyMap = moneyMap;
        }

        public Product[] getProducts() {
            return products;
        }

        public Map<Integer, Money> getMoneyMap() {
            return moneyMap;
        }

        public void addProduct(int id) {
            int qty = products[id].getQuantity();
            products[id].setQuantity(qty + 1);
        }

        public void addMoney(int nominal) {
            Money money = moneyMap.get(nominal);
            int count = money.getCount();
            money.setCount(count + 1);
        }

        public void clear() {
            for (int i = 1; i < products.length; i++) {
                products[i].setQuantity(0);
            }
            for (Money money: moneyMap.values()) {
                money.setCount(0);
            }
        }
    }
}
