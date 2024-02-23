/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT045MI.ConnCustExtPrc
 * Description : Connect customer external price.
 * Date         Changed By   Description
 * 20231004     ARENARD      GCOX02 - Connexion des prix externes aux clients
 * 20240221     ARENARD      lowerCamelCase fixed, max record = 1 added for readAll on OPRICH
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class ConnCustExtPrc extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    private final ProgramAPI program
    private final UtilityAPI utility
    private String prr1
    private String prr2
    private String prr3
    private String prr4
    private String prr5
    private String prr6
    private String cuc1
    private String cuc2
    private String cuc3
    private String cuc4
    private String cuc5
    private String cuc6
    private String lfl1
    private String lfl2
    private String lfl3
    private String lfl4
    private String lfl5
    private String lfl6

    public ConnCustExtPrc(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
        this.mi = mi
        this.database = database
        this.logger = logger
        this.program = program
        this.utility = utility
    }

    public void main() {
        Integer currentCompany
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer) program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }
        String currentDivision = program.getLDAZD().DIVI

        // Check customer
        String cuno = ""
        if (mi.in.get("CUNO") != null) {
            DBAction customerQuery = database.table("OCUSMA").index("00").build()
            DBContainer OCUSMA = customerQuery.getContainer()
            OCUSMA.set("OKCONO", currentCompany)
            OCUSMA.set("OKCUNO", mi.in.get("CUNO"))
            if (!customerQuery.read(OCUSMA)) {
                mi.error("Code client " + mi.in.get("CUNO") + " n'existe pas")
                return
            }
            cuno = mi.in.get("CUNO")
        }

        prr1 = (String) (mi.in.get("PRR1") != null ? mi.in.get("PRR1") : "");
        prr2 = (String) (mi.in.get("PRR2") != null ? mi.in.get("PRR2") : "");
        prr3 = (String) (mi.in.get("PRR3") != null ? mi.in.get("PRR3") : "");
        prr4 = (String) (mi.in.get("PRR4") != null ? mi.in.get("PRR4") : "");
        prr5 = (String) (mi.in.get("PRR5") != null ? mi.in.get("PRR5") : "");
        prr6 = (String) (mi.in.get("PRR6") != null ? mi.in.get("PRR6") : "");
        cuc1 = (String) (mi.in.get("CUC1") != null ? mi.in.get("CUC1") : "");
        cuc2 = (String) (mi.in.get("CUC2") != null ? mi.in.get("CUC2") : "");
        cuc3 = (String) (mi.in.get("CUC3") != null ? mi.in.get("CUC3") : "");
        cuc4 = (String) (mi.in.get("CUC4") != null ? mi.in.get("CUC4") : "");
        cuc5 = (String) (mi.in.get("CUC5") != null ? mi.in.get("CUC5") : "");
        cuc6 = (String) (mi.in.get("CUC6") != null ? mi.in.get("CUC6") : "");
        lfl1 = (String) (mi.in.get("LFL1") != null ? mi.in.get("LFL1") : "");
        lfl2 = (String) (mi.in.get("LFL2") != null ? mi.in.get("LFL2") : "");
        lfl3 = (String) (mi.in.get("LFL3") != null ? mi.in.get("LFL3") : "");
        lfl4 = (String) (mi.in.get("LFL4") != null ? mi.in.get("LFL4") : "");
        lfl5 = (String) (mi.in.get("LFL5") != null ? mi.in.get("LFL5") : "");
        lfl6 = (String) (mi.in.get("LFL6") != null ? mi.in.get("LFL6") : "");

        //   Check if all fields 1 are entered
        if (prr1 != "" || cuc1 != "" || lfl1 != "") {
            if (prr1 == "") {
                mi.error("Tarif 1 est obligatoire")
                return
            }
            if (cuc1 == "") {
                mi.error("Devise 1 est obligatoire")
                return
            }
            if (lfl1 == "") {
                mi.error("Etiquette 1 est obligatoire")
                return
            }
        }
        //   Check if all fields 2 are entered
        if (prr2 != "" || cuc2 != "" || lfl2 != "") {
            if (prr2 == "") {
                mi.error("Tarif 2 est obligatoire")
                return
            }
            if (cuc2 == "") {
                mi.error("Devise 2 est obligatoire")
                return
            }
            if (lfl2 == "") {
                mi.error("Etiquette 2 est obligatoire")
                return
            }
        }
        //   Check if all fields 3 are entered
        if (prr3 != "" || cuc3 != "" || lfl3 != "") {
            if (prr3 == "") {
                mi.error("Tarif 3 est obligatoire")
                return
            }
            if (cuc3 == "") {
                mi.error("Devise 3 est obligatoire")
                return
            }
            if (lfl3 == "") {
                mi.error("Etiquette 3 est obligatoire")
                return
            }
        }
        //   Check if all fields 4 are entered
        if (prr4 != "" || cuc4 != "" || lfl4 != "") {
            if (prr4 == "") {
                mi.error("Tarif 4 est obligatoire")
                return
            }
            if (cuc4 == "") {
                mi.error("Devise 4 est obligatoire")
                return
            }
            if (lfl4 == "") {
                mi.error("Etiquette 4 est obligatoire")
                return
            }
        }
        //   Check if all fields 5 are entered
        if (prr5 != "" || cuc5 != "" || lfl5 != "") {
            if (prr5 == "") {
                mi.error("Tarif 5 est obligatoire")
                return
            }
            if (cuc5 == "") {
                mi.error("Devise 5 est obligatoire")
                return
            }
            if (lfl5 == "") {
                mi.error("Etiquette 5 est obligatoire")
                return
            }
        }
        //   Check if all fields 6 are entered
        if (prr6 != "" || cuc6 != "" || lfl6 != "") {
            if (prr6 == "") {
                mi.error("Tarif 6 est obligatoire")
                return
            }
            if (cuc6 == "") {
                mi.error("Devise 6 est obligatoire")
                return
            }
            if (lfl6 == "") {
                mi.error("Etiquette 6 est obligatoire")
                return
            }
        }

        DBAction queryOPRICH = database.table("OPRICH").index("00").build()
        DBContainer OPRICH = queryOPRICH.getContainer()
        // Check Price list 1
        if (prr1 != "" && cuc1 != "") {
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr1)
            OPRICH.set("OJCUCD", cuc1)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 1 " + prr1 + " n'existe pas")
                    return
                }
            }
        }
        // Check Price list 2
        if (prr2 != "" && cuc2 != "") {
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr2)
            OPRICH.set("OJCUCD", cuc2)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 2 " + prr2 + " n'existe pas")
                    return
                }
            }
        }
        // Check Price list 3
        if (prr3 != "" && cuc3 != "") {
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr3)
            OPRICH.set("OJCUCD", cuc3)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 3 " + prr3 + " n'existe pas")
                    return
                }
            }
        }
        // Check Price list 4
        if (prr4 != "" && cuc4 != "") {
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr4)
            OPRICH.set("OJCUCD", cuc4)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 4 " + prr4 + " n'existe pas")
                    return
                }
            }
        }
        // Check Price list 5
        if (prr5 != "" && cuc5 != "") {
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr5)
            OPRICH.set("OJCUCD", cuc5)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 5 " + prr5 + " n'existe pas")
                    return
                }
            }
        }
        // Check Price list 6
        if (prr6 != "" && cuc6 != "") {
            // Check Price list 6
            OPRICH.set("OJCONO", currentCompany)
            OPRICH.set("OJPRRF", prr6)
            OPRICH.set("OJCUCD", cuc6)
            OPRICH.set("OJCUNO", cuno)
            if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                OPRICH.set("OJCUNO", "")
                if (!queryOPRICH.readAll(OPRICH, 4, 1, outDataOPRICH)) {
                    mi.error("Tarif 6 " + prr6 + " n'existe pas")
                    return
                }
            }
        }
        DBAction queryCSYTAB = database.table("CSYTAB").index("00").build()
        DBContainer CSYTAB = queryCSYTAB.getContainer()
        // Check label flag 1
        if (lfl1 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl1)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 1 " + lfl1 + " n'existe pas")
                return
            }
        }
        // Check label flag 2
        if (lfl2 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl2)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 2 " + lfl2 + " n'existe pas")
                return
            }
        }
        // Check label flag 3
        if (lfl3 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl3)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 3 " + lfl3 + " n'existe pas")
                return
            }
        }
        // Check label flag 4
        if (lfl4 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl4)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 4 " + lfl4 + " n'existe pas")
                return
            }
        }
        // Check label flag 5
        if (lfl5 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl5)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 5 " + lfl5 + " n'existe pas")
                return
            }
        }
        // Check label flag 6
        if (lfl6 != "") {
            CSYTAB.set("CTCONO", currentCompany)
            CSYTAB.set("CTDIVI", "")
            CSYTAB.set("CTSTCO", "LFLA")
            CSYTAB.set("CTSTKY", lfl6)
            CSYTAB.set("CTLNCD", "")
            if (!queryCSYTAB.read(CSYTAB)) {
                mi.error("Etiquette 6 " + lfl6 + " n'existe pas")
                return
            }
        }

        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query = database.table("OCUSEP").index("00").build()
        DBContainer OCUSEP = query.getContainer()
        OCUSEP.set("OGCONO", currentCompany)
        OCUSEP.set("OGDIVI", "")
        OCUSEP.set("OGCUNO", cuno)
        if(!query.readLock(OCUSEP, updateCallBack)){
            OCUSEP.set("OGPRR1", prr1);
            OCUSEP.set("OGPRR2", prr2);
            OCUSEP.set("OGPRR3", prr3);
            OCUSEP.set("OGPRR4", prr4);
            OCUSEP.set("OGPRR5", prr5);
            OCUSEP.set("OGPRR6", prr6);
            OCUSEP.set("OGCUC1", cuc1);
            OCUSEP.set("OGCUC2", cuc2);
            OCUSEP.set("OGCUC3", cuc3);
            OCUSEP.set("OGCUC4", cuc4);
            OCUSEP.set("OGCUC5", cuc5);
            OCUSEP.set("OGCUC6", cuc6);
            OCUSEP.set("OGLFL1", lfl1);
            OCUSEP.set("OGLFL2", lfl2);
            OCUSEP.set("OGLFL3", lfl3);
            OCUSEP.set("OGLFL4", lfl4);
            OCUSEP.set("OGLFL5", lfl5);
            OCUSEP.set("OGLFL6", lfl6);
            OCUSEP.setInt("OGRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            OCUSEP.setInt("OGRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            OCUSEP.setInt("OGLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            OCUSEP.setInt("OGCHNO", 1)
            OCUSEP.set("OGCHID", program.getUser())
            query.insert(OCUSEP)
        }
    }
    // Retrieve OPRICH
    Closure<?> outDataOPRICH = { DBContainer OPRICH ->
    }
    // Update OCUSEP
    Closure<?> updateCallBack = { LockedResult lockedResult ->
        LocalDateTime timeOfCreation = LocalDateTime.now()
        int changeNumber = lockedResult.get("OGCHNO")
        lockedResult.set("OGPRR1", prr1);
        lockedResult.set("OGPRR2", prr2);
        lockedResult.set("OGPRR3", prr3);
        lockedResult.set("OGPRR4", prr4);
        lockedResult.set("OGPRR5", prr5);
        lockedResult.set("OGPRR6", prr6);
        lockedResult.set("OGCUC1", cuc1);
        lockedResult.set("OGCUC2", cuc2);
        lockedResult.set("OGCUC3", cuc3);
        lockedResult.set("OGCUC4", cuc4);
        lockedResult.set("OGCUC5", cuc5);
        lockedResult.set("OGCUC6", cuc6);
        lockedResult.set("OGLFL1", lfl1);
        lockedResult.set("OGLFL2", lfl2);
        lockedResult.set("OGLFL3", lfl3);
        lockedResult.set("OGLFL4", lfl4);
        lockedResult.set("OGLFL5", lfl5);
        lockedResult.set("OGLFL6", lfl6);
        lockedResult.setInt("OGLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        lockedResult.setInt("OGCHNO", changeNumber + 1)
        lockedResult.set("OGCHID", program.getUser())
        lockedResult.update()
    }
}
