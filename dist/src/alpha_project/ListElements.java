
package alpha_project;

import java.util.Collection;
import javafx.collections.FXCollections;
import static javafx.collections.FXCollections.sort;
import javafx.collections.ObservableList;

/*
    This class is to be used with the backend methods for updating or relisting data.
    This class can return a data structure of DUMMY, STATIC or DYNAMIC data.
    @param <T> the type of the values being boxed.
*/

public class ListElements {

    //returns ObservableList containing Pairwise instances.
    public static ObservableList<Pair> getPairs() {
    /*    ObservableList<Pairwise> pairs = FXCollections.<Pairwise>observableArrayList();
        Pairwise pairA = new Pairwise(new Employee("employee", "B"), new Employee("employee", "D"), 200);
        Pairwise pairB = new Pairwise(new Employee("employee", "F"), new Employee("employee", "D"), 20);
        Pairwise pairC = new Pairwise(new Employee("employee", "K"), new Employee("employee", "D"), 100);
        Pairwise pairD = new Pairwise(new Employee("employee", "I"), new Employee("employee", "D"), 120);
        Pairwise pairE = new Pairwise(new Employee("employee", "E"), new Employee("employee", "D"), 40);
        Pairwise pairF = new Pairwise(new Employee("employee", "X"), new Employee("employee", "D"), 50);
        Pairwise pairG = new Pairwise(new Employee("employee", "Q"), new Employee("employee", "D"), 10);
        
        pairs.add(pairA);
        pairs.add(pairB);
        pairs.add(pairC);
        pairs.add(pairD);
        pairs.add(pairE);
        pairs.add(pairF);
        pairs.add(pairG);
    */    
        ObservableList<Pair> pairs = FXCollections.observableArrayList(Backend.pairRankings);
        
        return pairs;
    }
    
    
    //returns ObservableList containing Employee instances (DUMMY DATA)
    public static ObservableList<Efactfile> getEmployees() {
      /*
        ObservableList<Employee> employees = FXCollections.<Employee>observableArrayList();
        Employee employeeA = new Employee("employee", "A");
        Employee employeeB = new Employee("employee", "B");
        Employee employeeC = new Employee("employee", "C");
        Employee employeeD = new Employee("employee", "D");
        Employee employeeE = new Employee("employee", "E");
        Employee employeeF = new Employee("employee", "F");
        Employee employeeG = new Employee("employee", "G");
        Employee employeeH = new Employee("employee", "H");
        Employee employeeI = new Employee("employee", "I");
        Employee employeeJ = new Employee("employee", "J");
        Employee employeeK = new Employee("employee", "K");
        Employee employeeL = new Employee("employee", "L");
        Employee employeeM = new Employee("employee", "M");
        Employee employeeN = new Employee("employee", "N");
        Employee employeeO = new Employee("employee", "O");
        Employee employeeP = new Employee("employee", "P");
        

        employees.add(employeeA);
        employees.add(employeeB);
        employees.add(employeeC);
        employees.add(employeeD);
        employees.add(employeeE);
        employees.add(employeeF);
        employees.add(employeeG);
        employees.add(employeeH);
        employees.add(employeeI);
        employees.add(employeeJ);
        employees.add(employeeK);
        employees.add(employeeL);
        employees.add(employeeM);
        employees.add(employeeN);
        employees.add(employeeO);
        employees.add(employeeP);
*/
        ObservableList<Efactfile>employees = FXCollections.observableArrayList(Backend.EmployeeDisplay);
        
        return employees;
    }


    //returns ObservableList containing Group instances    (DUMMY DATA)
    public static ObservableList<Group> getGroups() {
       
        ObservableList<Group>groups = FXCollections.observableArrayList(Backend.GroupsDisplay);
        return groups;
    }
    
    /*public static String stringFormatter(String s1, String s2, int num) {
        //String of length max 40 using char array. 
        char[] myString;
        
        return myString.toString();
    }*/

    //private ObservableList<Employee> pullEmployees() {}
    //private ObservableList<Group> pullGroups() {}
}