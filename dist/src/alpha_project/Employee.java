
package alpha_project;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Employee {
    private StringProperty firstName;
    private StringProperty lastName;
    private ObservableList<Employee> employees = FXCollections.observableArrayList();
    //might need an array here for associated email addresses
    
    public StringProperty firstNameProperty() {
        if (firstName == null) {
            firstName = new SimpleStringProperty();
        }
        return firstName;
    }
    
    public StringProperty lastNameProperty() {
        if (lastName == null) {
            lastName = new SimpleStringProperty();
        }
        return lastName;
    }
    
    public ObservableList<Employee> employeesProperty() {
        return employees;
    }
    
    public final String getFirstName() {
        return firstNameProperty().get();
    }
    public final void setFirstName(String value) {
        firstNameProperty().set(value);
    }   
    public final String getLastName() {
        return lastNameProperty().get();
    }
    public final void setLastName(String value) {
        lastNameProperty().set(value);
    }
    
    public final String getFullName() {
        return firstNameProperty().get() + " " + lastNameProperty().get();
    }
    
    public Employee(String fname, String lname) {
        setFirstName(fname);
        setLastName(lname);
    }
    
}
