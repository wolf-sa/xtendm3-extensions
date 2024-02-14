/**
 * README
 * This extension is used by interface
 *
 * Name : EXT885MI.DelPartner
 * Description : Delete records from the CPARTN table (CRS885).
 * Date         Changed By   Description
 * 20221129     RENARN       REFX04 - API for CRS885
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class DelPartner extends ExtendM3Transaction {
    private final MIAPI mi
    private final LoggerAPI logger
    private final ProgramAPI program
    private final DatabaseAPI database
    private final SessionAPI session
    private final TransactionAPI transaction
    private final MICallerAPI miCaller
    private boolean IN60
    private Integer currentCompany
    private Integer XXPCTG

    public DelPartner(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
        this.mi = mi
        this.database = database
        this.program = program
        this.logger = logger
        this.miCaller = miCaller
    }

    public void main() {
        currentCompany
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer) program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }
        // Check partner type
        if (mi.in.get("PATE") == null) {
            mi.error("Type de partenaire est obligatoire")
            return
        } else {
            DBAction query = database.table("CPATYP").index("00").selection("CAPCTG").build()
            DBContainer CPATYP = query.getContainer()
            CPATYP.set("CACONO", currentCompany)
            CPATYP.set("CAPATE", mi.in.get("PATE"))
            if (!query.read(CPATYP)) {
                mi.error("Type de partenaire " + mi.in.get("PATE") + " n'existe pas")
                return
            } else {
                XXPCTG = CPATYP.get("CAPCTG")
            }
        }
        // Check partner ID
        if (mi.in.get("PAID") == null) {
            mi.error("ID partenaire est obligatoire")
            return
        }
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query = database.table("CPARTN").index("00").build()
        DBContainer CPARTN = query.getContainer()
        CPARTN.set("CHCONO", currentCompany)
        CPARTN.set("CHPCTG", XXPCTG)
        CPARTN.set("CHPAID", mi.in.get("PAID"))
        CPARTN.set("CHPAI1", mi.in.get("PAI1"))
        CPARTN.set("CHPAI2", mi.in.get("PAI2"))
        if(!query.readLock(CPARTN, updateCallBack)){
            mi.error("L'enregistrement n'existe pas")
            return
        }
    }

    Closure<?> updateCallBack = { LockedResult lockedResult ->
        lockedResult.delete()
    }
}