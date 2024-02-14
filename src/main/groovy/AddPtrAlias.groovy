/**
 * README
 * This extension is used by interface
 *
 * Name : EXT886MI.AddPtrAlias
 * Description : Add records to the CPAALI table (CRS886).
 * Date         Changed By   Description
 * 20221202     RENARN       REFX04 - API for CRS885
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddPtrAlias extends ExtendM3Transaction {
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
    private Integer XXPAAC
    private String XXPAAQ
    private String XXPAIH
    private String XXPATE

    public AddPtrAlias(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
        // Check partner category
        if (mi.in.get("PCTG") == null) {
            mi.error("Catégorie partenaire est obligatoire")
            return
        } else {
            XXPCTG = mi.in.get("PCTG")
            if (XXPCTG != 1 &&
                    XXPCTG != 2 &&
                    XXPCTG != 3 &&
                    XXPCTG != 10 &&
                    XXPCTG != 11 &&
                    XXPCTG != 12 &&
                    XXPCTG != 13 &&
                    XXPCTG != 21) {
                mi.error("Catégorie partenaire " + XXPCTG + " est invalide")
                return
            }
        }
        // Check partner ID
        if (mi.in.get("PAID") == null) {
            mi.error("ID partenaire est obligatoire")
            return
        }
        // Check partner
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query_CPARTN = database.table("CPARTN").index("00").selection("CHPAIH", "CHPATE").build()
        DBContainer CPARTN = query_CPARTN.getContainer()
        CPARTN.set("CHCONO", currentCompany)
        CPARTN.set("CHPCTG", XXPCTG)
        CPARTN.set("CHPAID", mi.in.get("PAID"))
        CPARTN.set("CHPAI1", mi.in.get("PAI1"))
        CPARTN.set("CHPAI2", mi.in.get("PAI2"))
        if (!query_CPARTN.read(CPARTN)) {
            mi.error("Partenaire n'existe pas")
            return
        } else {
            XXPAIH = CPARTN.get("CHPAIH")
            XXPATE = CPARTN.get("CHPATE")
        }
        // Check partner alias type
        if (mi.in.get("PAAT") == null) {
            mi.error("Type référence complémentaire partenaire est obligatoire")
            return
        } else {
            DBAction query_CPAALT = database.table("CPAALT").index("00").selection("CEPAAC", "CEPAAQ").build()
            DBContainer CPAALT = query_CPAALT.getContainer()
            CPAALT.set("CECONO", currentCompany)
            CPAALT.set("CEPAAT", mi.in.get("PAAT"))
            if (!query_CPAALT.read(CPAALT)) {
                mi.error("Type référence complémentaire partenaire " + mi.in.get("PAAT") + " n'existe pas")
                return
            } else {
                XXPAAC = CPAALT.get("CEPAAC")
                XXPAAQ = CPAALT.get("CEPAAQ")
            }
        }
        DBAction query = database.table("CPAALI").index("00").build()
        DBContainer CPAALI = query.getContainer()
        CPAALI.set("CKCONO", currentCompany)
        CPAALI.set("CKPCTG", XXPCTG)
        CPAALI.set("CKPAID", mi.in.get("PAID"))
        CPAALI.set("CKPAI1", mi.in.get("PAI1"))
        CPAALI.set("CKPAI2", mi.in.get("PAI2"))
        CPAALI.set("CKPAAC", XXPAAC)
        CPAALI.set("CKPAAQ", XXPAAQ)
        CPAALI.set("CKPAAL", mi.in.get("PAAL"))
        if (!query.read(CPAALI)) {
            CPAALI.set("CKPAIH", XXPAIH)
            CPAALI.set("CKPATE", XXPATE)
            CPAALI.set("CKPAAT", mi.in.get("PAAT"))
            CPAALI.setInt("CKRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CPAALI.setInt("CKRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            CPAALI.setInt("CKLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CPAALI.setInt("CKCHNO", 1)
            CPAALI.set("CKCHID", program.getUser())
            query.insert(CPAALI)
        } else {
            mi.error("L'enregistrement existe déjà")
            return
        }
    }
}