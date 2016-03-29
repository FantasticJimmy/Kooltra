public class Tools{
    public static void Create(String name, Integer versionNum, String text){
        Action__c newRecord = new Action__c();
        newRecord.Name = name;
        newRecord.Version__c = versionNum;
        newRecord.Text__c = text;
        insert newRecord;
        
        Action__c newAc = new Action__c();
        newAc = [SELECT Cus_ID__c,Version__c,Text__c,Name FROM Action__c ORDER by CreatedDate desc limit 1];
        Log__c newLog = new Log__c();
        newLog.Name = newAc.Cus_ID__c;
        newLog.Version_Log__c = newAc.Version__c;
        newLog.Text_Log__c = '"'+ newAc.Text__c +'"';
        newLog.Record_Name__c = newAc.Name;
        insert newLog;
    }

    

    public static void Modify(String id, String newText){
        Action__c selectedRecord;
        selectedRecord = [SELECT Name,Version__c,Text__c FROM Action__c WHERE Cus_ID__c = :id];
        
        Log__c selectedLog;
        selectedLog = [SELECT Record_Name__c,Version_Log__c,Text_Log__c FROM Log__c WHERE Name=:id];
        
        selectedRecord.Version__c += 1;
        selectedRecord.Text__c = newText;

        update selectedRecord;

        selectedLog.Version_Log__c = selectedRecord.Version__c;
        selectedLog.Text_Log__c += ',' + '"' + selectedRecord.Text__c + '"';
        update selectedLog;
    }
    
    public static void DeleteRecord(String id){
        Action__c doomedRecord = [SELECT Version__c,Text__c,Name FROM Action__c WHERE Cus_ID__c = :id];
            
        delete doomedrecord;
    }
    
    public static void RevertRecord(String id){
        Log__c targetLog = [SELECT Record_Name__c,Version_Log__c,Text_Log__c FROM Log__c WHERE Name=:id];
        Action__c RevertRecord = [SELECT Name,Version__c,Text__c FROM Action__c WHERE Cus_ID__c = :id];

        RevertRecord.Version__c = targetLog.Version_Log__c - 1;
        targetLog.Version_Log__c--;

        String[] targetLogArray = targetLog.Text_Log__c.split(',');
        Integer size = targetLogArray.size();
        
        RevertRecord.Text__c = targetLogArray[size-2];
        targetLogArray.remove(size-1);
        targetLog.Text_Log__c = String.join(targetLogArray,',');
        
        update targetLog;
        update RevertRecord;
    }

    public static void UndeleteRecord(String id){
        Log__c targetLog = [SELECT Record_Name__c,Version_Log__c,Text_Log__c,Name FROM Log__c WHERE Name=:id];
        Action__c UndeleteRecord = new Action__c();
        UndeleteRecord.Name = targetLog.Record_Name__c;
        UndeleteRecord.Version__c = targetLog.Version_Log__c;


        String[] targetLogArray = targetLog.Text_Log__c.split(',');
        Integer size = targetLogArray.size();
        
        UndeleteRecord.Text__c = targetLogArray[size-1];

        insert UndeleteRecord;
        
        Action__c newAc = [SELECT Cus_ID__c FROM Action__c ORDER by CreatedDate desc limit 1];
        targetLog.Name = newAc.Cus_ID__c;
        
        update targetLog;
    }

    public static void Archive(){
        
        Log__c[] fullLog = [SELECT Record_Name__c,Version_Log__c,Text_Log__c,Name FROM Log__c];
        try{TextDump__c d = [SELECT Dump__c FROM TextDump__c];
            d.Dump__c = '';
            for(Log__c item : fullLog){
                d.Dump__c += item.Record_Name__c + ','+ item.Version_Log__c+','+item.Text_Log__c+';';
            }  
            Update d;
        }
        catch(Exception e){ 
            TextDump__c d = new TextDump__c();
            d.Dump__c = '';
            for(Log__c item : fullLog){
                d.Dump__c += item.Record_Name__c + ','+ item.Version_Log__c+','+item.Text_Log__c+';';
            }  
            insert d;
        }
    }

    public static void Import(){
        TextDump__c fullLog = [SELECT Dump__c FROM TextDump__c];
        String[] targetImportArray = fullLog.Dump__c.split(';');
        Integer targetImportArraySize = targetImportArray.size();
        for(Integer i=0;i<targetImportArraySize;i++){
            String[] workingArray = targetImportArray[i].split(',');
            Integer workingArraySize = workingArray.size();
            Action__c newRecord = new Action__c();
            newRecord.Name = workingArray[0];
            newRecord.Version__c = Integer.valueOf(workingArray[1]);
            newRecord.Text__c = workingArray[workingArraySize-1];
            insert newRecord;
            
            Action__c newAc = new Action__c();
            newAc = [SELECT Cus_ID__c,Version__c,Text__c,Name FROM Action__c ORDER by CreatedDate desc limit 1];
            Log__c newLog = new Log__c();
            newLog.Name = newAc.Cus_ID__c;
            newLog.Version_Log__c = newAc.Version__c;
            newLog.Text_Log__c = '"'+ newAc.Text__c +'"';
            newLog.Record_Name__c = newAc.Name;
            insert newLog;
                
        }

    }
}