/**
 * README
 * This extension is used by interface
 *
 * Name : EXT885MI.AddPartner
 * Description : Add records to the CPARTN table (CRS885).
 * Date         Changed By   Description
 * 20221129     RENARN       REFX04 - API for CRS885
 * 20240221     RENARN       Useless timeOfCreation removed, method header comments added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddPartner extends ExtendM3Transaction {
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
    private String XXFILE
    private String XXKEY1
    private String XXKEY2
    private String XXKEY3
    private String XXPAID
    private String XXPAI1
    private String XXPAI2
    private String XXPAIH
    private String XXMSID

    public AddPartner(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
        // Check partner category
        if (mi.in.get("PAIH") != null) {
            if(XXPCTG != 11 && XXPCTG != 12 && XXPCTG != 13){
                mi.error("ID partenaire niveau supérieur non autorisé pour le type " + (mi.in.get("PATE")))
                return
            } else {
                DBAction query = database.table("CPARTN").index("00").build()
                DBContainer CPARTN = query.getContainer()
                CPARTN.set("CHCONO", currentCompany)
                CPARTN.set("CHPCTG", 10)
                CPARTN.set("CHPAID", mi.in.get("PAIH"))
                CPARTN.set("CHPAI1", "")
                CPARTN.set("CHPAI2", "")
                if (!query.read(CPARTN)) {
                    mi.error("ID partenaire niveau supérieur " + (mi.in.get("PAIH")) + " n'existe pas")
                    return
                }
            }
        }
        // Check partner ID
        if (mi.in.get("PAID") == null) {
            mi.error("ID partenaire est obligatoire")
            return
        } else {
            // Check partner ID 1
            if (mi.in.get("PAI1") != null) {
                XXPAI1 = mi.in.get("PAI1")
            }
            logger.debug("Step 1 - XXPAI1 = " + XXPAI1)
            // Check partner ID 2
            if (mi.in.get("PAI2") != null) {
                XXPAI2 = mi.in.get("PAI2")
            }
            logger.debug("Step 1 - XXPAI2 = " + XXPAI2)
            XXPAID = mi.in.get("PAID")
            XXPAID = XXPAID.trim()
            RVALA()
            if (XXMSID == "WDI0103") {
                mi.error("Société " + XXPAID + " n'existe pas")
                return
            }
            if (XXMSID == "WFAC303") {
                mi.error("Etablissement " + XXPAID + " n'existe pas")
                return
            }
            if (XXMSID == "WWH0103") {
                mi.error("Dépôt " + XXPAID + " n'existe pas")
                return
            }
            if (XXMSID == "WCU0203") {
                mi.error("Client " + XXPAID + " n'existe pas")
                return
            }
            if (XXMSID == "WCU3503") {
                mi.error("Circuit commercial " + XXPAID + " n'existe pas")
                return
            }
            if (XXMSID == "WAD1003") {
                mi.error("Adresse " + XXPAI1 + " n'existe pas")
                return
            }
            if (XXMSID == "WDL3803") {
                mi.error("Spécification de livraison " + XXPAI2 + " n'existe pas")
                return
            }
            if (XXMSID == "WSU0103") {
                mi.error("Fournisseur " + XXPAID + " n'existe pas")
                return
            }
        }
        // Check partner ID High level
        XXPAIH = ""
        if (mi.in.get("PAIH") != null) {
            XXPAIH = mi.in.get("PAIH")
        }
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query = database.table("CPARTN").index("00").build()
        DBContainer CPARTN = query.getContainer()
        CPARTN.set("CHCONO", currentCompany)
        CPARTN.set("CHPCTG", XXPCTG)
        CPARTN.set("CHPAID", mi.in.get("PAID"))
        CPARTN.set("CHPAI1", mi.in.get("PAI1"))
        CPARTN.set("CHPAI2", mi.in.get("PAI2"))
        if (!query.read(CPARTN)) {
            CPARTN.set("CHPATE", mi.in.get("PATE"))
            CPARTN.set("CHPAIH", XXPAIH)
            CPARTN.setInt("CHRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CPARTN.setInt("CHRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            CPARTN.setInt("CHLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CPARTN.setInt("CHCHNO", 1)
            CPARTN.set("CHCHID", program.getUser())
            query.insert(CPARTN)
        } else {
            mi.error("L'enregistrement existe déjà")
            return
        }
    }

    // Validate
    public void RVALA() {
        IN60 = false
        XXMSID = ""
        XXKEY1 = XXPAID
        if (XXPCTG == 1) {
            XXFILE = "CMNDIV"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 2) {
            XXFILE = "CFACIL"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 3) {
            XXFILE = "MITWHL"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 10) {
            XXFILE = "OCUSMA"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 11) {
            XXFILE = "OCUSMA"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 12) {
            XXKEY2 = XXPAI1
            XXFILE = "OCUSMA"
            RCHK()
            if (IN60) {
                return
            }
            XXFILE = "OCUSAD"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 13) {
            XXKEY2 = XXPAI1
            XXKEY3 = XXPAI2
            XXFILE = "OCUSMA"
            RCHK()
            if (IN60) {
                return
            }
            XXFILE = "OCUSAD"
            RCHK()
            if (IN60) {
                return
            }
            XXFILE = "OCUSAS"
            RCHK()
            if (IN60) {
                return
            }
        }
        if (XXPCTG == 21) {
            XXFILE = "CIDMAS"
            RCHK()
            if (IN60) {
                return
            }
        }
    }

    //Check file
    public void RCHK() {
        if (XXFILE == "CMNDIV") {
            DBAction query = database.table("CMNDIV").index("00").build()
            DBContainer CMNDIV = query.getContainer()
            CMNDIV.set("CCCONO", currentCompany)
            CMNDIV.set("CCDIVI", XXKEY1)
            if (!query.read(CMNDIV)) {
                IN60 = true
                XXMSID = "WDI0103"
            }
        }
        if (XXFILE == "CFACIL") {
            DBAction query = database.table("CFACIL").index("00").build()
            DBContainer CFACIL = query.getContainer()
            CFACIL.set("CFCONO", currentCompany)
            CFACIL.set("CFFACI", XXKEY1)
            if (!query.read(CFACIL)) {
                IN60 = true
                XXMSID = "WFAC303"
            }
        }
        if (XXFILE == "MITWHL") {
            DBAction query = database.table("MITWHL").index("00").build()
            DBContainer MITWHL = query.getContainer()
            MITWHL.set("MWCONO", currentCompany)
            MITWHL.set("MWWHLO", XXKEY1)
            if (!query.read(MITWHL)) {
                IN60 = true
                XXMSID = "WWH0103"
            }
        }
        if (XXFILE == "OCUSMA") {
            DBAction query = database.table("OCUSMA").index("00").build()
            DBContainer OCUSMA = query.getContainer()
            OCUSMA.set("OKCONO", currentCompany)
            OCUSMA.set("OKCUNO", XXKEY1)
            if (!query.read(OCUSMA)) {
                IN60 = true
                XXMSID = "WCU0203"
            }
        }
        if (XXFILE == "OCHSTR") {
            DBAction query = database.table("OCHSTR").index("00").build()
            DBContainer OCHSTR = query.getContainer()
            OCHSTR.set("OGCONO", currentCompany)
            OCHSTR.set("OGCHAI", XXKEY1)
            if (!query.read(OCHSTR)) {
                IN60 = true
                XXMSID = "WCU3503"
            }
        }
        if (XXFILE == "OCUSAD") {
            logger.debug("Step 2 - XXKEY1 = " + XXKEY1)
            logger.debug("Step 2 - XXKEY2 = " + XXKEY2)
            DBAction query = database.table("OCUSAD").index("00").build()
            DBContainer OCUSAD = query.getContainer()
            OCUSAD.set("OPCONO", currentCompany)
            OCUSAD.set("OPCUNO", XXKEY1)
            OCUSAD.set("OPADRT", 1)
            OCUSAD.set("OPADID", XXKEY2)
            if (!query.read(OCUSAD)) {
                IN60 = true
                XXMSID = "WAD1003"
            }
        }
        if (XXFILE == "OCUSAS") {
            DBAction query = database.table("OCUSAS").index("00").build()
            DBContainer OCUSAS = query.getContainer()
            OCUSAS.set("O2CONO", currentCompany)
            OCUSAS.set("O2CUNO", XXKEY1)
            OCUSAS.set("O2ADID", XXKEY2)
            OCUSAS.set("O2DLSP", XXKEY3)
            if (!query.read(OCUSAS)) {
                IN60 = true
                XXMSID = "WDL3803"
            }
        }
        if (XXFILE == "CIDMAS") {
            DBAction query = database.table("CIDMAS").index("00").build()
            DBContainer CIDMAS = query.getContainer()
            CIDMAS.set("IDCONO", currentCompany)
            CIDMAS.set("IDSUNO", XXKEY1)
            if (!query.read(CIDMAS)) {
                IN60 = true
                XXMSID = "WSU0103"
            }
        }
    }
}