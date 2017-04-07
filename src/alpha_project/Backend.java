package alpha_project;
import static alpha_project.Backend.EmployeeDisplay;
import static alpha_project.Backend.GroupsDisplay;
import static alpha_project.Backend.GroupsDisplay1;
import static alpha_project.Backend.adjlist;
import static alpha_project.Backend.basestructure;
import com.google.common.collect.*;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 *
 * @author Kameron
 */
public class Backend {

    /**
     * @param args the command line arguments
     */
  static String[] employeeList;  
  static HashMap<String,HashMap> adjlist; 
  static LinkedHashMultimap<String,Email> basestructure;
  static LinkedHashMultimap<String,Email> substructure; 
  static Pair[] pairRankings = new Pair[30];
  static ArrayList<Set<String>>cliques = new ArrayList<Set<String>>();
  static int averageVolume = 0;
  public static Collection<Group>GroupsDisplay = new ArrayList<Group>() {};
   static ArrayList<Group> GroupsDisplay1 = new ArrayList<Group>();
  static ArrayList<Efactfile> EmployeeDisplay1 = new ArrayList<Efactfile>();
  public static Collection<Efactfile> EmployeeDisplay = new ArrayList<Efactfile>();
   static ArrayList<Pair> pairs = new ArrayList();
  public static void runMain(String[] args) {
        
    basestructure = LinkedHashMultimap.create();
    
    importJson();
    
    // get list of employee names
    Set temp = basestructure.keySet();
    String x = temp.toString();
    String y = x.substring(1, x.length()-1);
    employeeList = y.split(", ");
    
    createStaticGraph(); //populate adjlist
    getPairRankings(); //get top 30 pairs
    GroupsDisplay1 = getBiggestMaximalCliques(); //get list of all groups
    //NEED TO SORT BY TOTAL VOLUME
    
    for( String empName : employeeList){
        Efactfile employee = new Efactfile(empName);
        EmployeeDisplay1.add(employee);
    }
    Collections.sort(EmployeeDisplay1, (Efactfile emp1, Efactfile emp2) -> emp1.employee.compareTo(emp2.employee));
    EmployeeDisplay = EmployeeDisplay1;
    
    
    Collections.sort(GroupsDisplay1, (Group p1, Group p2) -> p2.getEmailCount() - p1.getEmailCount());
    GroupsDisplay = GroupsDisplay1;
    
    
   java.util.Date fromDate = new Date(2001-1900, 12-1, 25); //25th Dec 1999
   java.util.Date toDate = new Date(2002-1900, 12-1, 25);   // 2th Dec 2000
   
   
   filterPairRankings(fromDate, toDate);
   int q = 1;
                  
              
    
  }
    //---------------------------------------------------------------------------------
  
     public static Collection<Set<String>> getAllMaximalCliques()
    {
        // TODO jvs 26-July-2005:  assert that graph is simple

        
        ArrayList<String> pc = new ArrayList<String>();
        ArrayList<String> cands = new ArrayList<String>(Arrays.asList(employeeList));
        ArrayList<String> found = new ArrayList<String>();
       ArrayList<String> empList = new ArrayList<String>(Arrays.asList(employeeList));
        findCliques(pc, cands, found);
        return cliques;
    }

  
     public static ArrayList<Group> getBiggestMaximalCliques()
    {
        getAllMaximalCliques();
        
        
        // get average edge weight.
        int edgeCount=0;
        for (String node :adjlist.keySet()){
            Collection<Edge> temp = adjlist.get(node).values();
            for( Edge edge : temp ){
                averageVolume += edge.weight;
                edgeCount++;
            }
        }
        averageVolume = (int) ((averageVolume/edgeCount)+0.5);

        Collection<Group> noSubCliques = new CopyOnWriteArrayList();
       int count =1;
        for (Set<String> clique : cliques) { // for each clique
          // for each cliique determine if volume between employees is above threshold
            
            int lowVol = 0;
            for (String emp1 : clique){
              for (String emp2 : clique){
                    if (emp1.equals(emp2)== false){
                        int vol  = ((Edge) adjlist.get(emp1).get(emp2)).weight;
                        if (vol < 2){   //arbitrary value
                         lowVol = 1;
                        }
                  
                 }
                }
            } 
           if (clique.size() > 2 && lowVol ==0)  { //clique.size()> sizeFilter;
               Group group = new Group(String.valueOf(count), clique);
               noSubCliques.add(group);
               count++;
            }
        }
        ArrayList<Group> returnGroups = new ArrayList<Group>();
        for (Group group : noSubCliques){
            returnGroups.add(group);
        }
 
        return returnGroups;
    }

    private static void findCliques(ArrayList<String> potential_clique, ArrayList<String> cands, ArrayList<String> already_found)
    {
        
        ArrayList<String> candArr = new ArrayList<String>(cands);
         ArrayList<String> candList = new ArrayList<String>(cands);
        
        if (!end(candList, already_found)) {
          
            for (String candidate : candArr) {
                ArrayList<String> new_candidates = new ArrayList<String>();
                ArrayList<String> new_already_found = new ArrayList<String>();

                potential_clique.add(candidate);
                candList.remove(candidate);

                for (String new_candidate : candList) {
                    if (adjlist.get(candidate).containsKey(new_candidate)&& adjlist.get(new_candidate).containsKey(candidate)){
                        new_candidates.add(new_candidate);
                    } 
                } 
                for (String new_found : already_found) {
                     if (adjlist.get(candidate).containsKey(new_found)&& adjlist.get(new_found).containsKey(candidate)){
                        new_already_found.add(new_found);
                    } 
                }
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
                   
                    cliques.add(new HashSet<String>(potential_clique));
                } 
                else {
                    findCliques(
                        potential_clique,
                        new_candidates,
                        new_already_found);
                } 

                already_found.add(candidate);
                potential_clique.remove(candidate);
            } 
        } 
    }

    private static boolean end(ArrayList<String> candidates, ArrayList<String> already_found)
    {
        boolean end = false;
        int edgecounter;
        for (String found : already_found) {
            edgecounter = 0;
            for (String candidate : candidates) {
                 if (adjlist.get(found).containsKey(candidate) && adjlist.get(candidate).containsKey(found)){
                    edgecounter++;
                } 
            } 
            if (edgecounter == candidates.size()) {
                end = true;
            }
        } 
        return end;
    }

  
    
  private static Edge getPairwiseComms(String emp1, String emp2){
      Edge emails;
      if(adjlist.get(emp1)== null){
          HashMap<String,Edge> edges = new HashMap <String,Edge>();
          adjlist.put(emp1, edges);
      }
      
      if (adjlist.get(emp1).containsKey(emp2)){
          
           emails = (Edge)(adjlist.get(emp1)).get(emp2);
      } else {
           return null;
      }
      return emails;
  }
  
  public static Pair[] filterPairRankings(Date fromDate, Date toDate){
      ArrayList<Pair> filteredPairs = new ArrayList<Pair>();
      for (Pair pair : pairs){
          Pair filteredPair = pair.getPairByTime(fromDate, toDate);
          filteredPairs.add(filteredPair);
      }
    
      Collections.sort(filteredPairs, (Pair p1, Pair p2) -> p2.getVolume() - p1.getVolume());
      Pair[] filteredPairRankings = new Pair[30];
      for ( int i =0; i <filteredPairs.size(); i++){
        if (i < 30){
            filteredPairRankings[i] = filteredPairs.get(i);
        }
    }
      return filteredPairRankings;
  }
  
  
  
  private static void getPairRankings(){
    Object z = getPairwiseComms("j..farmer@enron.com","z" );
   
    for (int i = 0; i < employeeList.length; i++){ // for eah node
        for ( Object key : adjlist.get(employeeList[i]).keySet()){ // for each edge
            Pair pair = new Pair(employeeList[i], (String)key);
            boolean toInsert = true;
            for (Pair pair1 : pairs) {
                if (pair1.isOpposite(pair)) {
                    toInsert = false;
                }
            }
            if(toInsert == true){
                pairs.add(pair);
            }
        }
    }
    for(int i = 0; i<pairs.size(); i++){
        String emp1 = pairs.get(i).emp1;
        String emp2 = pairs.get(i).emp2;
        Edge Edge1 = getPairwiseComms(emp1, emp2);
        Edge Edge2 = getPairwiseComms(emp2, emp1);
        
        LinkedList<Date> emp1Sent;
        if (Edge1 != null){
            emp1Sent = Edge1.datelist;
        }
        else {
            emp1Sent = null;
        }
        LinkedList<Date> emp2Sent;
        if (Edge2 != null){
            emp2Sent = Edge2.datelist;
        }
        else {
            emp2Sent = null;
        }
        
        pairs.get(i).setDateLists(emp1Sent, emp2Sent);
    }
    
   for (int i = 0; i < pairs.size(); i++){
        for(int j = i; j < pairs.size(); j++){
            if (pairs.get(i).emp1.equals(pairs.get(j).emp2) && pairs.get(i).emp2.equals( pairs.get(j).emp1)){
                pairs.remove(j);
            }
        }
    }
    
      Collections.sort(pairs, (Pair p1, Pair p2) -> p2.getVolume() - p1.getVolume());
    
    for ( int i =0; i <pairs.size(); i++){
        if (i < 30){
            pairRankings[i] = pairs.get(i);
        }
       averageVolume += pairs.get(i).getVolume();
    }
    averageVolume = averageVolume/pairs.size();
  }
  
  private static void createStaticGraph(){
    adjlist = new HashMap<String, HashMap>();
    for ( int i = 0; i<employeeList.length; i++ ){
        

        Set<Email> tempy = basestructure.get(employeeList[i]);
        LinkedList<String> recipients = new LinkedList();
        HashMap<String,Edge> egdes = new HashMap <String,Edge>(); 
        int count = 0;
        for (Email email: tempy){
            if (recipients.contains(email.getReciever())== false){
                recipients.add(email.getReciever());
                Edge edge = new Edge(email.getSender(),email.getReciever(), email.getDate());
                egdes.put(email.getReciever(), edge);
            }
            else{
                Edge edge = egdes.get(email.getReciever());
                edge.increaseWieght(email.getDate());
                egdes.put(email.getReciever(), edge);
                //*****MAYBE DO ORIGINAL MULTIMAP WITHIN HASHMAP IDEA LIKE THIS
            } 
            count++;
            if (count == tempy.size()){
                adjlist.put(email.getSender(), egdes);
                break;
            }
            
        }

    }        
    
    
  }
  
  public static Collection<Efactfile> getCentralityRanking(){
      
      Collections.sort(EmployeeDisplay1, new Comparator<Efactfile>() {
        @Override public int compare(Efactfile emp1, Efactfile emp2) {
            return emp2.getnoGroups() - emp1.getnoGroups();
        }
    });
      
      
       Collection<Efactfile> centrality = new ArrayList<Efactfile>();
       centrality = EmployeeDisplay1;
      return centrality;
  }
    
    
  private static void importJson(){
    
    JSONParser parser = new JSONParser();

   
        try{
				System.out.println("Here!");
                 JSONArray a = (JSONArray) parser.parse(new FileReader(System.getProperty("user.dir")+"\\src\\alpha_project\\enron.json"));

		 for (Object o : a)
                {
                    JSONObject emaillist = (JSONObject) o;

                    String sender = (String) emaillist.get("sender");
                    //System.out.println(sender);
                   
                    String timestamp = (String) emaillist.get("timestamp");
                    String dateString = timestamp.substring(0, 10);
                    String res[] = dateString.split("-");
                    int year = Integer.parseInt(res[0]);
                    int month = Integer.parseInt(res[1]);
                    int day =  Integer.parseInt(res[2]);
                    java.util.Date date = new Date(year-1900, month-1, day);
                  
                    
                    JSONArray recipients = (JSONArray) emaillist.get("recipients");

                    for (Object r : recipients)
                    {
                       // System.out.println(r+"");
                        String reciever = r.toString();
                        if (reciever.equals(sender)){
                            break;
                        }
                        if (reciever.contains("@enron.com") == true){       // ensures on messages between employee emails are counted
                            String[]sen = sender.split("@");
                            String[]rec = reciever.split("@");
                            Email email = new Email(sen[0],rec[0],date);
                             
                            basestructure.put(email.sender, email);
                        }
                    }
                    
                }
                        
        } catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	}
    
} 
}


class Edge {
    public String sender;
    public String recipient;
    public int weight;
    public LinkedList<Date> datelist;
            
    public Edge(String sender, String recipient, Date date) {
                this.sender = sender;
                this.recipient = recipient;
                this.weight = 1;
                this.datelist = new LinkedList<Date>();
                this.datelist.add(date);
            }
            
    public Edge(String sender, String recipient, LinkedList<Date> dates) {
                this.sender = sender;
                this.recipient = recipient;
                this.weight = 1;
                this.datelist = new LinkedList<Date>();
                this.datelist = dates;
            }
            
    public void increaseWieght(Date date){
                this.weight++;
                this.datelist.add(date);
            }         
            
    public Edge getEdgeByTime(Edge oldEdge, Date fromDate, Date toDate){
                LinkedList<Date> filteredDates = new LinkedList<Date>();
                for (Date date : oldEdge.datelist){
                    if (date.after(fromDate) && date.before(toDate)){
                        filteredDates.add(date);
                    }
                }
                Edge newEdge = new Edge(sender, recipient, filteredDates);
                return newEdge;
            }
            
    @Override
    public String toString(){
        
        String kkfes= (this.recipient + "    Email Count: "+datelist.size() );
        return kkfes;
       
    }
            
}

class Efactfile{
    public String employee;
    public ArrayList<Edge> comms = new ArrayList<Edge>();
    public ArrayList<Group> participating = new ArrayList<Group>();
    public Edge[] highestContacted = new Edge[3];
    public int Groupcount;
    public Date dateFrom = null;
    public Date dateTo = null;

    public Efactfile(String employee){
        this.employee = employee;  
        for ( Object key : adjlist.get(employee).keySet()){
            Edge edge = (Edge) adjlist.get(employee).get(key);
            comms.add(edge);
        }
        
       Collections.sort(comms, (Edge p1, Edge p2) -> p2.datelist.size() - p1.datelist.size());
        for (int i=0; i<3; i++){
            try {
            highestContacted[i]=comms.get(i);
            } catch (Exception e){
                
            }
        }
        Groupcount=0;
        for(Group group : GroupsDisplay1){
            if (group.isMember(employee) == true)
                Groupcount++;                   
        }
        for (Group group : GroupsDisplay1){
            if (group.isMember(employee)== true){
               participating.add(group);
            }
        }
        

    } //for static employee
    
    public Efactfile (String employee, Date fromDate, Date toDate, ArrayList<Group> part){
        this.dateFrom = fromDate;
        this.dateTo = toDate;
        this.employee = employee;  
        for ( Object key : adjlist.get(employee).keySet()){
            Edge edge = (Edge) adjlist.get(employee).get(key);
            Edge dateEdge = edge.getEdgeByTime(edge, fromDate, toDate);
            if (dateEdge.datelist.size() > 0){
                comms.add(dateEdge);
            }
        }
        
       Collections.sort(comms, (Edge p1, Edge p2) -> p2.datelist.size() - p1.datelist.size());
        for (int i=0; i<3; i++){
            try {
            highestContacted[i]=comms.get(i);
            } catch (Exception e){
                
            }
        }
        Groupcount=0;
        for(Group group : GroupsDisplay1){
            if (group.isMember(employee) == true)
                Groupcount++;                   
        }
        this.participating = part;
        
        
    } // for date range employee
    

    public int getnoEmail(){
       int emailsent =0;
       for (Edge edge : comms){
           emailsent+= edge.datelist.size();
       }
       return emailsent;
    }
    
    public Edge[] mostContacted (){      
        return highestContacted;
    }
    
    public int getnoGroups(){     
        return Groupcount;
    }
    
    public ArrayList<Group> getGroups(){
        return participating;
    }
    
    public graphData[] getGraphData(){
        ArrayList<graphData> data = new ArrayList<graphData>();
        ArrayList<Date> alldates = new ArrayList<Date>();
        for (Edge edge : comms){
            for (Date date : edge.datelist){
                alldates.add(date);
            }
        }
        ArrayList<Date> uniqueDates = new ArrayList<Date>(new HashSet<Date>(alldates));
          Collections.sort(uniqueDates, new Comparator<Date>() {
        @Override public int compare(Date date1, Date date2) {
            return date1.compareTo(date2);
        }
        });
        
        graphData[] returnArray = new graphData[uniqueDates.size()];
        for ( int i = 0; i < returnArray.length; i++){
            returnArray[i] = new graphData(uniqueDates.get(i),0);
        }
        

        
        for (Edge edge : comms){
            for (Date date : edge.datelist){
                String day = new SimpleDateFormat("dd-MM-yyyy").format(date);
                for ( int i = 0; i < returnArray.length; i++){
                    if (day.equals(returnArray[i].day)){
                        returnArray[i].increaseCount();
                    }
                }
            }
        }
        
        return returnArray;
    }

}

class graphData{
    public String day;
    public int count;
    
    public graphData(Date date, int count){
        
         day = new SimpleDateFormat("dd-MM-yyyy").format(date);
        this.count = count;
    }
    public void increaseCount(){
        this.count++;
    }
    
}



class Group {
    public String name;
    public ArrayList<String> members = new ArrayList<String>();
    public ArrayList<Pair> pairs = new ArrayList<Pair>();
    public ArrayList<Edge> communications = new ArrayList<Edge>();
    public Date fromDate = null;
    public Date toDate = null;
    
    public Group(String name, Set<String> clique){
        this.name  =name;
        for (String emp : clique){
            members.add(emp);
        }
        
        for (int i = 0; i < members.size(); i++){
            for (int j =0; j<members.size(); j++){
                if (i != j ){
                    Edge edge = (Edge)(adjlist.get(members.get(i))).get(members.get(j));
                    communications.add(edge);
                    Edge oppEdge = (Edge)(adjlist.get(members.get(j))).get(members.get(i));
                    Pair pair = new Pair(members.get(i), members.get(j));
                    pair.setDateLists(edge.datelist, oppEdge.datelist);
                    int toInsert = 1;
                    if (pairs.isEmpty()){
                        pairs.add(pair);
                    } else {
                        for (int k = 0; k< pairs.size(); k++){
                            if(pairs.get(k).isOpposite(pair)){
                                toInsert = 0;
                            }
                        }
                        if(toInsert == 1){
                            pairs.add(pair);
                        }
                    }
                }
            }
        }
        

    }
    
   public Group (Group grp, Date fromDate, Date toDate){
     this.fromDate = fromDate;
     this.toDate = toDate;
     this.name  =grp.name;
     this.members = grp.members;
        
        for (int i = 0; i < members.size(); i++){
            for (int j =0; j<members.size(); j++){
                if (i != j ){
                    Edge edge = (Edge)(adjlist.get(members.get(i))).get(members.get(j));
                    Edge dateEdge = edge.getEdgeByTime(edge, fromDate, toDate);
                    if (dateEdge.datelist.size() > 0 ){
                    communications.add(dateEdge);
                    Edge oppEdge = (Edge)(adjlist.get(members.get(j))).get(members.get(i));
                     Edge oppDateEdge = oppEdge.getEdgeByTime(oppEdge, fromDate, toDate);
                    Pair pair = new Pair(members.get(i), members.get(j));
                    pair.setDateLists(dateEdge.datelist, oppDateEdge.datelist);
                    int toInsert = 1;
                    if (pairs.isEmpty()){
                        if (pair.getVolume()> 0){
                        pairs.add(pair);
                        }
                    } else {
                        for (int k = 0; k< pairs.size(); k++){
                            if(pairs.get(k).isOpposite(pair)){
                                toInsert = 0;
                            }
                        }
                        if(toInsert == 1){
                            if(pair.getVolume() > 0){
                            pairs.add(pair);
                        }
                    }
                }
                    }
            }
        }
    
        }

    } // for date range employee

    
        public graphData[] getGraphData(){
        ArrayList<graphData> data = new ArrayList<graphData>();
        ArrayList<Date> alldates = new ArrayList<Date>();
        for (Edge edge : communications){
            for (Date date : edge.datelist){
                alldates.add(date);
            }
        }
        ArrayList<Date> uniqueDates = new ArrayList<Date>(new HashSet<Date>(alldates));
        
        Collections.sort(uniqueDates, new Comparator<Date>() {
        @Override public int compare(Date date1, Date date2) {
            return date1.compareTo(date2);
        }
        });
        
        graphData[] returnArray = new graphData[uniqueDates.size()];
        for ( int i = 0; i < returnArray.length; i++){
            returnArray[i] = new graphData(uniqueDates.get(i),0);
        }
        

        
        for (Edge edge : communications){
            for (Date date : edge.datelist){
                String day = new SimpleDateFormat("dd-MM-yyyy").format(date);
                for ( int i = 0; i < returnArray.length; i++){
                    if (day.equals(returnArray[i].day)){
                        returnArray[i].increaseCount();
                    }
                }
            }
        }
        
        return returnArray;
    }
    
    public int getEmailCount(){
        int count = 0;
        for (Edge edge : communications){
            count += edge.datelist.size();
        }
        return count;
    }
    
    public Pair[] getTopPairs(){
                ArrayList<Pair>temp = new ArrayList<Pair>();
                temp = pairs;
        Collections.sort(temp, new Comparator<Pair>() {
        @Override public int compare(Pair p1, Pair p2) {
            return p2.getVolume()- p1.getVolume();
        }
                });
        Pair[] topPairs = new Pair[3];
        for (int i = 0; i < pairs.size(); i++){
            try {
                topPairs[i] = temp.get(i);
            } catch (Exception e){
                
            }
        }
        return topPairs;
        //sort pairs by volume and return top 3
    }
    
    public int countMembers(){
        return members.size();
    }
    
    public boolean isMember(String employee){
         for(int i=0; i<members.size();i++){
             if (members.get(i).equals(employee)==true)
                 return true;
         }
        return false;
     }
    
    public Collection<Efactfile> getEmployees(){
        Collection<Efactfile> employeeObjArray = new ArrayList<Efactfile>();
        for (String emp : members){
            for ( Efactfile empObj : EmployeeDisplay){
                if (emp.equals( empObj.employee)){
                    employeeObjArray.add(empObj);
                    break;
                }
            }
        }
        return employeeObjArray;
    }
     
}


class Pair{
    public String emp1;
    public String emp2;
    public LinkedList<Date> emp1Sent;
    public LinkedList<Date> emp2Sent;

    public Pair(String emp1, String emp2){
        super();
        this.emp1=emp1;
        this.emp2=emp2;
    }
    
    public Pair(String emp1, String emp2, LinkedList<Date> emp1Sent, LinkedList<Date> emp2Sent){
        super();
        this.emp1=emp1;
        this.emp2=emp2;
        this.emp1Sent = emp1Sent;
        this.emp2Sent = emp2Sent;
    }
    
    public int getVolume(){
        int x;
        int y;
        if (emp1Sent == null){
             x = 0;
        } else{
             x = emp1Sent.size();
        }
        if (emp2Sent == null){
             y = 0;
        } else{
             y = emp2Sent.size();
        }
        return x+y;
    }
    
    public Pair getPairByTime(Date fromDate, Date toDate){
            LinkedList<Date> filteredEmp1 = new LinkedList<Date>();
            LinkedList<Date> filteredEmp2 = new LinkedList<Date>();
        
        if (emp1Sent != null ){
            for (Date date : emp1Sent){
                    if (date.after(fromDate) && date.before(toDate)){
                        filteredEmp1.add(date);
                    }
                }
        }
            if (emp2Sent != null){
            for (Date date : emp2Sent){
                    if (date.after(fromDate) && date.before(toDate)){
                        filteredEmp2.add(date);
                    }
                }
        }    
            Pair newPair = new Pair(emp1, emp2, filteredEmp1, filteredEmp2);
            return newPair;
    }
    
    
    public boolean isOpposite(String emp1,String emp2){
        if (emp1 == this.emp2 && emp2 == this.emp1){
            return true;
        }
        return false;
    }
    public boolean isOpposite(Pair pair){
        if (pair.emp1 == this.emp2 && pair.emp2 == this.emp1){
            return true;
        }
        return false;
    }
   
    public void setDateLists(LinkedList<Date> dates1,LinkedList<Date> dates2 ){
        this.emp1Sent = dates1;
        this.emp2Sent = dates2;
    }
    
    @Override
    public String toString(){
        
        String kkfes= this.emp1 + "  ---  "+ this.emp2+"  "+getVolume();
        return kkfes;
       
    }
  
}


class Email{
    public String sender;
    private String receipient;
    private Date date;

    public Email(String sender, String receipient, Date date){
        super();
        this.sender=sender;
        this.receipient=receipient;
        this.date=date;
        //this.next=next;
    
    
    }
    
    String getSender(){
        return this.sender;
    }
     String getReciever(){
        return this.receipient;
    }
     Date getDate(){
         return this.date;
     }

  @Override
    public String toString(){
        
        String kkfes= this.receipient + ": Date "+ this.date;
        return kkfes;
       
    }
}