import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
//        Scanner scan = new Scanner(new File("input.txt"));
        Stack<Integer> stack = new Stack<>();
        Map<Integer, String> stackElements = new HashMap<>();
        Map<Integer, List<String>> attributeNames = new HashMap<>();
        Map<Integer, List<String>> attributeValues = new HashMap<>();
        Integer lastRemoved = 0;
        boolean shouldBeGoodTag = false;

        while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("<?")) {
                shouldBeGoodTag = true;
                continue;
            }
            boolean shouldClose = false;
            if (shouldBeGoodTag) {
                if (trimmedLine.startsWith("<")) {
                    trimmedLine = trimmedLine.substring(1);
                }
                if (trimmedLine.endsWith("/>")) {
                    trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 2);
                    shouldBeGoodTag = false;
                    shouldClose = true;
                } else if (trimmedLine.endsWith(">")) {
                    trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 1);
                    shouldBeGoodTag = true;
                }
                int index = trimmedLine.indexOf(" ");
                String elemName = index == -1 ? trimmedLine : trimmedLine.substring(0, index);
                String attrLine = index == -1 ? "" : trimmedLine.substring(index).trim(); //trimming to remove possible whitespace in the start
                boolean idFound = false;
                boolean valueFound = false;
                String value = null;
                Integer id = null;
                Integer parentId = stack.isEmpty() ? null : stack.peek();

                List<String> attrNames = new ArrayList<>();
                List<String> attrValues = new ArrayList<>();
                while (!attrLine.isEmpty()) {
                    index = attrLine.indexOf("=");
                    String attrName = attrLine.substring(0, index);
                    char quotationUsed = attrLine.charAt(index + 1);
                    int endIndex = attrLine.indexOf(quotationUsed, index + 2);
                    String attrValue = attrLine.substring(index + 2, endIndex);
                    attrNames.add(attrName);
                    attrValues.add(attrValue);
                    if ("id".equals(attrName)) {
                        idFound = true;
                        id = Integer.valueOf(attrValue);
                        stack.push(id);
                    }
                    if ("value".equals(attrName)) {
                        valueFound = true;
                        value = attrValue;
                    }
                    attrLine = attrLine.substring(endIndex + 1).trim(); //trimming to remove possible whitespace in the start
                }

                if (idFound) {
                    System.out.println((parentId == null ? "null" : parentId) + " " + id + (!valueFound ? "" : " " + value));
                } else { //root element in sample
                    id = 0;
                    stack.push(id);
                }
                stackElements.put(id, elemName);
                attributeNames.put(id, attrNames);
                attributeValues.put(id, attrValues);
                if (shouldClose) {
                    lastRemoved = stack.pop();
                }
            } else {
                if (trimmedLine.startsWith("</")) {
                    shouldBeGoodTag = false;
                    String elem = trimmedLine.substring(2, trimmedLine.length() - 1);
                    lastRemoved = stack.pop();
//                    if (!stackElements.get(lastRemoved).equals(elem)) {
//                        System.out.println("===========ERROR!!!!!!!");
//                    }
                    continue;
                }
                if (trimmedLine.startsWith("<")) {
                    trimmedLine = trimmedLine.substring(1);
                }
                if (trimmedLine.endsWith("/>")) {
                    trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 2);
                    shouldClose = true;
                    shouldBeGoodTag = false;
                } else if (trimmedLine.endsWith(">")) {
                    trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 1);
                    shouldBeGoodTag = true;
                }
                String elemName = stackElements.get(lastRemoved);
                if (trimmedLine.startsWith(elemName)) {
                    String attrLine = trimmedLine.substring(elemName.length());
                    boolean idFound = false;
                    boolean valueFound = false;
                    Integer id = null;
                    String value = null;
                    Integer parentId = stack.isEmpty() ? null : stack.peek();
                    List<String> attrNames = new ArrayList<>();
                    List<String> attrValues = new ArrayList<>();
                    while (!attrLine.isEmpty()) {
                        int index = attrLine.lastIndexOf("=");
                        if (index == -1) {
                            break;
                        }
                        String attrValue = attrLine.substring(index + 1);
                        attrLine = attrLine.substring(0, index);
                        String attrName = null;
                        int matchCount = 0;
                        List<String> patternList = attributeNames.get(lastRemoved);
                        for (String attr: patternList) {
                            if (attrLine.endsWith(attr)) {
                                matchCount++;
                                attrName = attr;
                            }
                        }
                        if (matchCount == 0) {
                            if (!patternList.contains("value")) {
                                if (attrLine.endsWith("value")) {
                                    attrName = "value";
                                    matchCount++;
                                }
                            }
                        }
                        if (matchCount == 0) { // we fail to detect
                            continue;
                        }
                        if (matchCount > 1) { //skip for now
                            continue;
                        }
                        if ("id".equals(attrName)) {
                            idFound = true;
                            id = Integer.valueOf(attrValue);
                            stack.push(id);
                        }
                        if ("value".equals(attrName)) {
                            valueFound = true;
                            value = attrValue;
                        }
                        attrNames.add(attrName);
                        attrValues.add(attrValue);
                        attrLine = attrLine.substring(0, attrLine.length() - attrName.length());
                    }
                    if (idFound) {
                        System.out.println((parentId == null ? "null" : parentId) + " " + id + (!valueFound ? "" : " " + value));
                        stackElements.put(id, elemName);
                        attributeNames.put(id, attrNames);
                        attributeValues.put(id, attrValues);

                    }
                    if (shouldClose) {
                        lastRemoved = stack.pop();
                    }
                } else { //assumption failed, miss the string
                    continue;
                }
                //System.out.println(trimmedLine);
            }
        }
    }
}
