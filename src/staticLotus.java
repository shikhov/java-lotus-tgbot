import lotus.domino.*;

import java.util.Calendar;

public class staticLotus {
    Session session;

    public int postComment(String server, String replicaID, String docUNID, String text, String chat_id) {
        try {
            NotesThread.sinitThread();
            session = NotesFactory.createSession();

            Database db = session.getDatabase(null, null);
            if (db.openByReplicaID(server, replicaID)) {
                String username = db.getView("($PersonalSettingsByTgID)").getDocumentByKey(chat_id).getItemValueString("docAuthor");
                Document emp = session.getDatabase("s075", "employees").getView("($mailAddresses)").getDocumentByKey(username);
                String commonUsername = session.createName(username).getCommon();
                Document task = db.getDocumentByUNID(docUNID);
                if (!task.isValid()) {
                    throw new NotesException(555, "Task document is a stub");
                }

                Document comment = db.createDocument();
                comment.appendItemValue("taskUnid", docUNID);
                comment.appendItemValue("Form", "Комментарий");
                comment.appendItemValue("author", commonUsername);
                comment.appendItemValue("authorUserName", username);
                comment.appendItemValue("dept", emp.getItemValueString("dept_txt"));
                comment.appendItemValue("deptshort", emp.getItemValueString("dept_short"));
                comment.appendItemValue("authorEmpUnid", emp.getUniversalID());
                comment.appendItemValue("authorDispname", emp.getItemValueString("dispname"));
                comment.appendItemValue("authorFirstName", emp.getItemValueString("firstname"));
                comment.appendItemValue("authorLastName", emp.getItemValueString("lastname"));
                comment.appendItemValue("authorMidName", emp.getItemValueString("midname"));
                RichTextItem rti = comment.createRichTextItem("Body");
                rti.appendText(text);
                if (comment.save()) {
                    System.err.println(Calendar.getInstance().getTime() + " New comment from " + commonUsername);

                    Agent agent = db.getAgent("(NewCommentNotification)");
                    agent.run(comment.getNoteID());

                    agent.recycle();
                }
                else {
                    System.err.println(Calendar.getInstance().getTime() + " Unable to save comment");
                }

                emp.recycle();
                task.recycle();
                comment.recycle();
                rti.recycle();
            }
            db.recycle();
            session.recycle();
        } catch(NotesException e) {
            System.err.println(Calendar.getInstance().getTime() + " " + e.text);
            return 1;
        } catch (Exception e) {
            System.err.println(Calendar.getInstance().getTime());
            System.err.println(e.getMessage());
            return 1;
        } finally {
            NotesThread.stermThread();
        }
        return 0;
    }

}