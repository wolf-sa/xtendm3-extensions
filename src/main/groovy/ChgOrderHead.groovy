/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.ChgOrderHead
 * Description : Change order head
 * Date         Changed By   Description
 * 20231003     ARENARD      GCOX01 – Mise à jour des champs paramétrables dans les en-têtes de commande
 * 20231102     ARENARD      New input parameter TEPA
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class ChgOrderHead extends ExtendM3Transaction {
    private final MIAPI mi
    private final LoggerAPI logger
    private final ProgramAPI program
    private final DatabaseAPI database
    private final SessionAPI session
    private final TransactionAPI transaction
    private final MICallerAPI miCaller
    private final UtilityAPI utility
    private String lncd

    public ChgOrderHead(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, LoggerAPI logger) {
        this.mi = mi
        this.database = database
        this.program = program
        this.miCaller = miCaller
        this.utility = utility
        this.logger = logger
    }

    public void main() {
        logger.info("Début performActualJob")
        Integer currentCompany
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer)program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }

        // Retrieve language from order head
        String lncd = ""
        DBAction OOHEAD_query = database.table("OOHEAD").index("00").selection("OAORNO","OALNCD").build()
        DBContainer OOHEAD = OOHEAD_query.getContainer()
        OOHEAD.set("OACONO", currentCompany)
        OOHEAD.set("OAORNO", mi.in.get("ORNO"))
        if(OOHEAD_query.read(OOHEAD)){
            lncd = OOHEAD.get("OALNCD")
        }

        // Check packaging term
        if(mi.in.get("TEPA") != null){
            DBAction query = database.table("CSYTAB").index("00").build()
            DBContainer CSYTAB = query.getContainer()
            CSYTAB.set("CTCONO",currentCompany)
            CSYTAB.set("CTDIVI",  "")
            CSYTAB.set("CTSTCO",  "TEPA")
            CSYTAB.set("CTSTKY", mi.in.get("TEPA"))
            CSYTAB.set("CTLNCD",  lncd)
            if (!query.read(CSYTAB)) {
                mi.error("Modalité de conditionnement " + mi.in.get("TEPA") + " n'existe pas")
                return
            }
        }

        // Update OOHEAD
        Closure<?> updateCallBack = { LockedResult lockedResult ->
            LocalDateTime timeOfCreation = LocalDateTime.now()
            int changeNumber = lockedResult.get("OACHNO")
            if (mi.in.get("UCA1") != null) lockedResult.set("OAUCA1", mi.in.get("UCA1"));
            if (mi.in.get("UCA2") != null) lockedResult.set("OAUCA2", mi.in.get("UCA2"));
            if (mi.in.get("UCA3") != null) lockedResult.set("OAUCA3", mi.in.get("UCA3"));
            if (mi.in.get("UCA4") != null) lockedResult.set("OAUCA4", mi.in.get("UCA4"));
            if (mi.in.get("UCA5") != null) lockedResult.set("OAUCA5", mi.in.get("UCA5"));
            if (mi.in.get("UCA6") != null) lockedResult.set("OAUCA6", mi.in.get("UCA6"));
            if (mi.in.get("UCA7") != null) lockedResult.set("OAUCA7", mi.in.get("UCA7"));
            if (mi.in.get("UCA8") != null) lockedResult.set("OAUCA8", mi.in.get("UCA8"));
            if (mi.in.get("UCA9") != null) lockedResult.set("OAUCA9", mi.in.get("UCA9"));
            if (mi.in.get("UCA0") != null) lockedResult.set("OAUCA0", mi.in.get("UCA0"));
            if (mi.in.get("UDN1") != null) lockedResult.setDouble("OAUDN1", mi.in.get("UDN1") as Double);
            if (mi.in.get("UDN2") != null) lockedResult.setDouble("OAUDN2", mi.in.get("UDN2") as Double);
            if (mi.in.get("UDN3") != null) lockedResult.setDouble("OAUDN3", mi.in.get("UDN3") as Double);
            if (mi.in.get("UDN4") != null) lockedResult.setDouble("OAUDN4", mi.in.get("UDN4") as Double);
            if (mi.in.get("UDN5") != null) lockedResult.setDouble("OAUDN5", mi.in.get("UDN5") as Double);
            if (mi.in.get("UDN6") != null) lockedResult.setDouble("OAUDN6", mi.in.get("UDN6") as Double);
            if (mi.in.get("UID1") != null) lockedResult.setInt("OAUID1", mi.in.get("UID1") as Integer);
            if (mi.in.get("UID2") != null) lockedResult.setInt("OAUID2", mi.in.get("UID2") as Integer);
            if (mi.in.get("UID3") != null) lockedResult.setInt("OAUID3", mi.in.get("UID3") as Integer);
            if (mi.in.get("UCT1") != null) lockedResult.set("OAUCT1", mi.in.get("UCT1"));
            if (mi.in.get("TEPA") != null) lockedResult.set("OATEPA", mi.in.get("TEPA"));
            lockedResult.setInt("OALMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            lockedResult.setInt("OACHNO", changeNumber + 1)
            lockedResult.set("OACHID", program.getUser())
            lockedResult.update()
        }

        if(!OOHEAD_query.readLock(OOHEAD, updateCallBack)) {
            mi.error("Le numéro de commande " + mi.in.get("ORNO") + " n'existe pas")
        }
    }
}