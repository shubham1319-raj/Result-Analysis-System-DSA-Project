import java.io.*;
import java.util.*;

class Student {
    String name;
    int[] marks;          // marks of 3 subjects

    Student(String name, int[] marks) {
        this.name = name;
        this.marks = marks;
    }

    int total() {
        return marks[0] + marks[1] + marks[2];
    }

    double percentage() {
        return total() / 3.0;
    }

    String grade() {
        double p = percentage();
        if (p >= 90) return "A+";
        if (p >= 75) return "A";
        if (p >= 60) return "B";
        if (p >= 40) return "C";
        return "F";
    }

    String percentText() {
        return String.format(Locale.US, "%.1f", percentage());
    }

    String fullLine() {
        return name + " | Total: " + total() + " | " + percentText() + "% | Grade " + grade();
    }
}

public class ResultAnalysisSystem {

    static final String FILE_NAME = "results.txt";
    static ArrayList<Student> students = new ArrayList<>();     // all results
    static HashMap<String, Student> byName = new HashMap<>();   // name lookup
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadFromFile();                    // load data at start

        int choice;
        do {
            printMenu();
            choice = readChoice();
            switch (choice) {
                case 1: addResult(); break;
                case 2: searchByName(); break;
                case 3: rankList(); break;
                case 4: topperAndAverage(); break;
                case 5: break;
                default: System.out.println("Invalid choice. Enter 1 to 5.");
            }
        } while (choice != 5);

        saveToFile();                      // save data on exit
        System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
    }

    static void printMenu() {
        System.out.println();
        System.out.println("===== RESULT ANALYSIS SYSTEM =====");
        System.out.println("1. Add result");
        System.out.println("2. Search by name");
        System.out.println("3. Rank list");
        System.out.println("4. Topper & average");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    static int readChoice() {
        if (!sc.hasNextLine()) return 5;
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* ---------- 1. Add result ---------- */
    static void addResult() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty() || name.contains(",")) {
            System.out.println("Invalid name. Name cannot be empty or contain a comma.");
            return;
        }
        if (byName.containsKey(name.toLowerCase())) {
            System.out.println(name + " is already added.");
            return;
        }

        System.out.print("Marks: ");
        String[] parts = sc.nextLine().trim().split("[,\\s]+");   // "80, 90, 70" or "80 90 70"
        if (parts.length != 3) {
            System.out.println("Invalid input. Enter exactly 3 marks.");
            return;
        }

        int[] marks = new int[3];
        for (int i = 0; i < 3; i++) {
            try {
                marks[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter numbers only.");
                return;
            }
            if (marks[i] < 0 || marks[i] > 100) {
                System.out.println("Invalid marks. Enter marks between 0 and 100.");
                return;
            }
        }

        Student s = new Student(name, marks);
        students.add(s);
        byName.put(name.toLowerCase(), s);
        System.out.println("Result added. " + s.fullLine());
    }

    /* ---------- 2. Search by name (HashMap) ---------- */
    static void searchByName() {
        System.out.print("Enter name to search: ");
        String name = sc.nextLine().trim();
        Student s = byName.get(name.toLowerCase());
        if (s == null) {
            System.out.println("Student not found.");
        } else {
            System.out.println(s.fullLine());
        }
    }

    /* ---------- 3. Rank list (sorted by total, highest first) ---------- */
    static void rankList() {
        if (students.isEmpty()) {
            System.out.println("No results available.");
            return;
        }

        ArrayList<Student> sorted = new ArrayList<>(students);    // copy
        insertionSort(sorted);

        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && sorted.get(i).total() < sorted.get(i - 1).total()) {
                rank = i + 1;                                     // same total = same rank
            }
            Student s = sorted.get(i);
            System.out.println(rank + ". " + s.name + " | " + s.total() + " | " + s.percentText() + "%");
        }
    }

    static void insertionSort(ArrayList<Student> list) {
        for (int i = 1; i < list.size(); i++) {
            Student key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).total() < key.total()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    /* ---------- 4. Topper & class average ---------- */
    static void topperAndAverage() {
        if (students.isEmpty()) {
            System.out.println("No results available.");
            return;
        }

        Student topper = students.get(0);
        double sum = 0;
        for (Student s : students) {
            if (s.total() > topper.total()) topper = s;
            sum += s.percentage();
        }
        System.out.println("Topper: " + topper.name + " (" + topper.total() + ")");
        System.out.println("Class average: " + String.format(Locale.US, "%.1f", sum / students.size()) + "%");
    }

    /* ---------- File handling ---------- */
    static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;
                try {
                    int[] marks = {Integer.parseInt(p[1].trim()),
                                   Integer.parseInt(p[2].trim()),
                                   Integer.parseInt(p[3].trim())};
                    Student s = new Student(p[0].trim(), marks);
                    students.add(s);
                    byName.put(s.name.toLowerCase(), s);
                } catch (NumberFormatException e) {
                    // skip bad line
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ".");
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                pw.println(s.name + "," + s.marks[0] + "," + s.marks[1] + "," + s.marks[2]);
            }
        } catch (IOException e) {
            System.out.println("Could not save to " + FILE_NAME + ".");
        }
    }
}