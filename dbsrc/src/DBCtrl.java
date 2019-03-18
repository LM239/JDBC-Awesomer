import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DBCtrl extends DBConn {

    @FXML TextField machineExerciseName, machineExerciseMachine, machineExerciseWeight, machineExerciseReps;
    @FXML TextField exerciseName;
    @FXML TextArea exerciseDesc;
    @FXML ChoiceBox workoutForm, workoutPerf;
    @FXML TextField workoutDuration, workoutExercise, workoutHour, workoutMin;
    @FXML TextArea workoutExercises;
    @FXML DatePicker workoutDate;
    @FXML TextField numberOfWorkouts;
    @FXML TextArea workoutNote;

    @FXML TextField exerciseGroup;
    @FXML DatePicker fromDate, toDate;
    @FXML TextField intervalExercise;
    @FXML TextField targetGroup, exerciseToAdd;
    @FXML TextField targetMachine;
    @FXML TextField machineToAdd;
    @FXML TextArea commandOutput;
    @FXML TextField machineToAddDesc;

    public DBCtrl() {
        System.out.println("test");
        this.connect();
    }

    public void addMachineExercise() {
        String machineName = machineExerciseMachine.getText();
        String exerciseName = machineExerciseName.getText();
        int vekt = Integer.parseInt(machineExerciseWeight.getText());
        int sett = Integer.parseInt(machineExerciseReps.getText());
        try {
            PreparedStatement statement1 = conn.prepareStatement("SELECT apparatsid FROM apparat WHERE navn = (?)");
            statement1.setString(1,machineName);
            ResultSet rs1 = statement1.executeQuery();
            rs1.next();
            int apparatsid = rs1.getInt("apparatsid");
            PreparedStatement statement2 = conn.prepareStatement("INSERT INTO øvelse (navn) VALUES ((?))");
            statement2.setString(1,exerciseName);
            statement2.execute();
            PreparedStatement statement3 = conn.prepareStatement("SELECT øvelseid FROM øvelse WHERE navn=(?)");
            statement3.setString(1,exerciseName);
            ResultSet rs2 = statement3.executeQuery();
            rs2.next();
            int øvelseid = rs2.getInt("øvelseid");
            PreparedStatement statement4 = conn.prepareStatement("INSERT INTO apparatsøvelse (øvelseid, apparatsid, antallkilo, antallsett) VALUES ((?), (?), (?), (?))");
            statement4.setInt(1,øvelseid);
            statement4.setInt(2,apparatsid);
            statement4.setInt(3,vekt);
            statement4.setInt(4,sett);
            statement4.execute();
            output("Apparatsøvelse '" + exerciseName + "' lagt til med " + vekt + " antall kilo og " + sett + " antall sett.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addMachine() {
        String machineName = machineToAdd.getText();
        String machineDesc = machineToAddDesc.getText();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO  apparat (navn, beskrivelse) VALUES ((?), (?))");
            ps.setString(1, machineName);
            ps.setString(2, machineDesc);
            ps.execute();
            output("Apparat '" + machineName + "' lagt til!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addExercise() {
        String name = exerciseName.getText();
        String description = exerciseDesc.getText();
        try {
            PreparedStatement statement1 = conn.prepareStatement("INSERT INTO øvelse (navn) VALUES ( (?) )");
            statement1.setString(1,name);
            statement1.execute();
            PreparedStatement statement2 = conn.prepareStatement("SELECT øvelseid FROM øvelse WHERE øvelse.navn = (?)");
            statement2.setString(1,name);
            ResultSet rs = statement2.executeQuery();
            rs.next();
            int øvelseid = rs.getInt("øvelseid");
            PreparedStatement statement = conn.prepareStatement("INSERT INTO ikkeapparatsøvelse VALUES ( (?), (?) )");
            statement.setInt(1,øvelseid);
            statement.setString(2,description);
            statement.execute();
            output("Exercise '" + name + "' added!");
            exerciseName.setText("");
            exerciseDesc.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWorkout() {
        int minutes = Integer.parseInt(workoutMin.getText());
        int hours = Integer.parseInt(workoutHour.getText());
        if (hours > 23 || hours < 0 || minutes > 59 || minutes < 0) {
            output("Ugyldig klokkeslett");
            return;
        }
        String datetime = workoutDate.getValue().toString() + " " + workoutHour.getText() + ":" + workoutMin.getText() + ":00";
        String note = workoutNote.getText();
        int perf = Integer.parseInt(workoutPerf.getValue().toString());
        int form = Integer.parseInt(workoutForm.getValue().toString());
        int duration = Integer.parseInt(workoutDuration.getText());
        String[] exercises = workoutExercises.getText().trim().split("\n");
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO treningsøkt (timestamp, varighet, form, prestasjon, notattekst) VALUES ((?), (?), (?), (?), (?))");
            statement.setString(1,datetime);
            statement.setInt(2,duration);
            statement.setInt(3,form);
            statement.setInt(4,perf);
            statement.setString(5,note);
            statement.execute();
            PreparedStatement statement1 = conn.prepareStatement("SELECT treningsøktid FROM treningsøkt ORDER BY treningsøktid DESC LIMIT 1");
            ResultSet rs = statement1.executeQuery();
            rs.next();
            int id = rs.getInt("treningsøktid");
            for (int i = 0; i < exercises.length; i++) {
                PreparedStatement loopValue = conn.prepareStatement("Select øvelseid FROM øvelse WHERE navn=(?)");
                loopValue.setString(1, exercises[i]);
                ResultSet rs1 = loopValue.executeQuery();
                rs1.next();
                PreparedStatement loopStatement = conn.prepareStatement("INSERT INTO haddeøvelse (treningsøktid, øvelseid) VALUES ((?),(?))");
                loopStatement.setInt(1, id);
                loopStatement.setInt(2,rs1.getInt("øvelseid"));
                loopStatement.execute();

            }
            output("Treningsøkt " + datetime + " er registrert med øvelsene");
            for (String øvelse : exercises) {
                output(øvelse);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getLatestWorkouts() {
        String workoutLimit = numberOfWorkouts.getText();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT timestamp, varighet, form, prestasjon, notattekst FROM treningsøkt ORDER BY timestamp DESC LIMIT ?");
            ps.setInt(1, Integer.parseInt(workoutLimit));
            ResultSet rs = ps.executeQuery();
            output("Her er data fra di siste " + workoutLimit + " øktene:");
            printWorkout(rs);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printWorkout(ResultSet rs) throws SQLException {
        while (rs.next()) {
            output("Dato: " + rs.getString("timestamp"));
            output("Varighet: " + rs.getString("varighet"));
            output("Form: " + rs.getString("form"));
            output("Prestasjon: " + rs.getString("prestasjon"));
            output("Notat: " + rs.getString("notattekst"));
            output("+-------------------------------------------------+");
        }
    }

    public void getExerciseGroup() {
        try {
            String groupName = exerciseGroup.getText();
            PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT øvelse.navn FROM (øvelsegruppe INNER JOIN inkludererøving ON (øvelsegruppe.gruppeid = inkludererøving.gruppeid)) INNER JOIN øvelse ON (inkludererøving.øvelseid=øvelse.øvelseid) WHERE (øvelsegruppe.navn=(?)) ORDER BY øvelse.navn DESC");
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            output("Gruppen '" + groupName + "' inneholder øvelsene:");
            while (rs.next()) {
                output(rs.getString("navn"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void createExerciseGroup() {
        String groupName = exerciseGroup.getText();
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT  INTO øvelsegruppe (navn) VALUES ((?))");
            statement.setString(1,groupName);
            statement.execute();
            output("Gruppen '" + groupName + "' ble opprettet");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getExerciseFromInterval() {
        String dateFrom = fromDate.getValue().toString() + " 00:00:00";
        String dateTo = toDate.getValue().toString() + " 23:59:59";
        Date ddateTo = new Date();
        Date ddateFrom = new Date();
        try {
            ddateTo = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateTo);
            ddateFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateFrom);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(dateFrom + "  " + dateTo);

        if (toDate.getValue().isBefore(fromDate.getValue())) {
            output("Tidsintervallet er ugyldig");
            return;
        }
        String exerciseName = intervalExercise.getText();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT  timestamp, varighet, form, prestasjon, " +
                    "notattekst FROM (øvelse INNER JOIN haddeøvelse ON (øvelse.øvelseid=haddeøvelse.øvelseid))" +
                    " INNER JOIN treningsøkt ON (treningsøkt.treningsøktid=haddeøvelse.treningsøktid) WHERE (" +
                    " (øvelse.navn=?) AND (treningsøkt.timestamp between ? AND ?))");
            statement.setString(1,exerciseName);
            statement.setDate(2, new java.sql.Date(ddateFrom.getTime()));
            statement.setDate(3, new java.sql.Date(ddateTo.getTime()));
            System.out.println(statement.toString());
            ResultSet rs = statement.executeQuery();
            output("Her er data fra øktene:");
            printWorkout(rs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addExerciseToGroup() {
        String exerciseName = exerciseToAdd.getText();
        String groupName = targetGroup.getText();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT gruppeid FROM øvelsegruppe WHERE navn=(?)");
            statement.setString(1, groupName);
            ResultSet rs = statement.executeQuery();
            PreparedStatement statement1 = conn.prepareStatement("SELECT øvelseid FROM øvelse WHERE navn=(?)");
            statement1.setString(1, exerciseName);
            ResultSet rs1 = statement1.executeQuery();
            rs1.next();
            rs.next();
            int øvelseid = rs1.getInt("øvelseid");
            int gruppeid = rs.getInt("gruppeid");
            PreparedStatement finalStatement = conn.prepareStatement("INSERT INTO inkludererøving (gruppeid, øvelseid) VALUES ((?),(?))");
            finalStatement.setInt(1, gruppeid);
            finalStatement.setInt(2, øvelseid);
            finalStatement.execute();
            output("'" + exerciseName + "' er lagt til i '" + groupName + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getExerciseNumb(){
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT count(navn) AS Antall FROM apparat INNER JOIN apparatsøvelse ON " +
                    "(apparat.apparatsid=apparatsøvelse.apparatsid) WHERE (apparat.navn=(?))");
            ps.setString(1, targetMachine.getText());
            ResultSet rs = ps.executeQuery();
            rs.next();
            output("Apparatet '" + targetMachine.getText() + "' inngår i " + rs.getInt("Antall") + " øvelse(r)");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addExerciseToList() {
        workoutExercises.appendText(workoutExercise.getText() + "\n");
        workoutExercise.setText("");
    }

    public void clearExercises() {
        workoutExercises.clear();
        workoutExercises.appendText(workoutDate.getValue().toString() + " " + workoutHour.getText() + ":" + workoutMin.getText() + ":00");
    }

    private void output(String output) {
        commandOutput.appendText(output + "\n");
    }

}
