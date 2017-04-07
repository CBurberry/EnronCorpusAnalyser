package alpha_project;

import java.util.ArrayList;
import java.util.Date;

public class Controller {
	private ArrayList<String> instances = new ArrayList<>(30);
	private ArrayList<Date> dateFrom = new ArrayList<>(30);
        private ArrayList<Date> dateTo = new ArrayList<>(30);
        private ArrayList<Efactfile> assignedE = new ArrayList<>(30);
        private ArrayList<Group> assignedG = new ArrayList<>(30);
	
	public void addPair(String s1, Date s2, Date s3) {
		instances.add(s1);
		dateFrom.add(s2);
                dateTo.add(s3);
	}
        
        public void assignEmployee(Efactfile emp, int winID) {
            assignedE.add(winID, emp);
            assignedG.add(winID, null);
        }
        
        
        public void assignGroup(Group grp, int winID) {
            assignedE.add(winID, null);
            assignedG.add(winID, grp);
        }
        
        public int size() {
            return instances.size();
        }
	
	public String getPair(int x) {
		return this.instances.get(x) + ": " + this.dateFrom.get(x) 
                        + " - " + this.dateTo.get(x);
	}
        
        public static boolean isIdentical(String a, String b) {
            if (a.equals(b)) {
                return true;
            }
            else {
                return false;
            }
        }
        
        public static String[] parseDate(String date) {
            //String format: YYYY-MM-DD
            char[] parser4 = new char[4];
            char[] parser2 = new char[2];
            String[] returnable = new String[3];
            String year = "";
            String month = "";
            String day = "";
            
            parser4[0] = date.charAt(0);
            parser4[1] = date.charAt(1);
            parser4[2] = date.charAt(2);
            parser4[3] = date.charAt(3);
            year = new String(parser4);
            
            
            parser2[0] = date.charAt(5);
            parser2[1] = date.charAt(6);
            month = new String(parser2);
            
            parser2[0] = date.charAt(8);
            parser2[1] = date.charAt(9);
            day = new String(parser2);

            returnable[0] = year;
            returnable[1] = month;
            returnable[2] = day;
            
            return returnable;
        }
        
        public ArrayList<String> getInstances() {return this.instances;}
        public void setInstances(ArrayList<String> override) {this.instances = override;}
        public ArrayList<Date> getDatesFrom() {return this.dateFrom;}
        public ArrayList<Date> getDatesTo() {return this.dateTo;}
        public void setDatesFrom(ArrayList<Date> override) {this.dateFrom = override;}
        public void setDatesTo(ArrayList<Date> override) {this.dateTo = override;}
        public void setDateFrom(Date override, int winID) {this.dateFrom.set(winID, override);}
        public void setDateTo(Date override, int winID) {this.dateTo.set(winID, override);}
        public Date getDateFrom(int winID) {return this.dateFrom.get(winID);}
        public Date getDateTo(int winID) {return this.dateTo.get(winID);}
        public String getInstanceNumber(int winID) {return this.instances.get(winID);}
        public Efactfile getInstanceEmp(int winID) {return this.assignedE.get(winID);}
        public Group getInstanceGroup(int winID) {return this.assignedG.get(winID);}
	
	Controller() {
		this.instances = new ArrayList<>(30);
		this.dateFrom = new ArrayList<>(30);
                this.dateTo = new ArrayList<>(30);
		instances.add("main");
                Date min = new Date(1997-1900, 01-1, 01);
                Date max = new Date(2002-1900, 06-1, 22);
		dateFrom.add(min);
                dateTo.add(max);
	}
}