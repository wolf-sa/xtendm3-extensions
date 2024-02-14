/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT423MI.ChangePackage
 * Description : Management of additional package information
 * Date         Changed By   Description
 * 20221115     RENARN       REFX03 - Management of additional package information
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class ChangePackage extends ExtendM3Transaction {
    private final MIAPI mi;
    private final LoggerAPI logger;
    private final ProgramAPI program
    private final DatabaseAPI database;
    private final SessionAPI session;
    private final TransactionAPI transaction

    public ChangePackage(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
        this.mi = mi;
        this.database = database
        this.program = program
    }

    public void main() {
        Integer currentCompany
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer)program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query = database.table("MPTRNS").index("00").build()
        DBContainer MPTRNS = query.getContainer()
        MPTRNS.set("ORCONO", currentCompany)
        MPTRNS.set("ORDIPA",  mi.in.get("DIPA"))
        MPTRNS.set("ORWHLO",  mi.in.get("WHLO"))
        MPTRNS.set("ORDLIX",  mi.in.get("DLIX") as Long)
        MPTRNS.set("ORPANR",  mi.in.get("PANR"))
        if(!query.readLock(MPTRNS, updateCallBack)){
            mi.error("L'enregistrement n'existe pas")
            return
        }
    }
    Closure<?> updateCallBack = { LockedResult lockedResult ->
        LocalDateTime timeOfCreation = LocalDateTime.now()
        int changeNumber = lockedResult.get("ORCHNO")
        if (mi.in.get("PAN1") != null)
            lockedResult.set("ORPAN1", mi.in.get("PAN1"))
        if (mi.in.get("PAN2") != null)
            lockedResult.set("ORPAN2", mi.in.get("PAN2"))
        if (mi.in.get("PAN3") != null)
            lockedResult.set("ORPAN3", mi.in.get("PAN3"))
        lockedResult.setInt("ORLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        lockedResult.setInt("ORCHNO", changeNumber + 1)
        lockedResult.set("ORCHID", program.getUser())
        lockedResult.update()
    }
}
