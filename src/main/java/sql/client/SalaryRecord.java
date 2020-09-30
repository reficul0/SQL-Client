package sql.client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

public class SalaryRecord implements Serializable {
    int id;
    String subject_name;
    double salary;
    double withheld;
    double issued;

    SalaryRecord(
            int number,
            String subject_name,
            double salary,
            double withheld,
            double issued
    ) {

        this.id = number;
        this.subject_name = subject_name;
        this.salary = salary;
        this.withheld = withheld;
        this.issued = issued;
    }

    @Override
    public String toString() {
        return String.format(
                "SalaryRecord{id=%d, subject_name=%s, salary=%f, withheld=%f, issued=%f}",
                id, subject_name, salary, withheld, issued
        );
    }

    public String[] toStringArray() {
        Vector<String> string_vector = new Vector<>();
        string_vector.add(String.valueOf(id));
        string_vector.add(String.valueOf(subject_name));
        string_vector.add(String.valueOf(salary));
        string_vector.add(String.valueOf(withheld));
        string_vector.add(String.valueOf(issued));
        Object[] objects_array = string_vector.toArray();
        return Arrays.copyOf(objects_array, objects_array.length, String[].class);
    }
}
