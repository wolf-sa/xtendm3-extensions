/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT025MI.UpdAlias
 * Description : Update alias number in MMS025.
 * Date         Changed By   Description
 * 20240103     ARNREN       REFX05 API pour MMS025
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdAlias extends ExtendM3Transaction {
    private final MIAPI mi
    private final LoggerAPI logger
    private final ProgramAPI program
    private final DatabaseAPI database
    private final SessionAPI session
    private final TransactionAPI transaction
    private final MICallerAPI miCaller
    private final UtilityAPI utility
    private final TextFilesAPI textFiles
    private Integer currentCompany
    private Integer alwt
    private String alwq
    private String itno
    private String popn
    private String e0pa
    private String sea1
    private String vfdt
    private Integer seqn
    private String logFileName

    public UpdAlias(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, TextFilesAPI textFiles) {
        this.mi = mi
        this.database = database
        this.program = program
        this.miCaller = miCaller
        this.utility = utility
        this.textFiles = textFiles
    }

    public void main() {
        currentCompany
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer) program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }

        if (mi.in.get("ALWT") != "" && mi.in.get("ALWT") != null) {
            alwt = mi.in.get("ALWT") as Integer
        } else {
            mi.error("Catégorie référence complémentaire est obligatoire")
            return
        }

        if (mi.in.get("ALWQ") != "" && mi.in.get("ALWQ") != null) {
            alwq = mi.in.get("ALWQ")
        }

        if (mi.in.get("ITNO") != "" && mi.in.get("ITNO") != null){
            itno = mi.in.get("ITNO")
            DBAction query = database.table("MITMAS").index("00").selection("MMUNMS").build()
            DBContainer MITMAS = query.getContainer()
            MITMAS.set("MMCONO", currentCompany)
            MITMAS.set("MMITNO",  mi.in.get("ITNO"))
            if (!query.read(MITMAS)) {
                mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
                return
            }
        } else {
            mi.error("Code article est obligatoire")
            return
        }

        if (mi.in.get("POPN") != "" && mi.in.get("POPN") != null) {
            popn = mi.in.get("POPN")
        } else {
            mi.error("Référence complémentaire est obligatoire")
            return
        }

        if (mi.in.get("E0PA") != "" && mi.in.get("E0PA") != null)
            e0pa = mi.in.get("E0PA")

        if (mi.in.get("SEA1") != "" && mi.in.get("SEA1") != null) {
            sea1 = mi.in.get("SEA1")
            DBAction query = database.table("CSEAMA").index("00").build()
            DBContainer CSEAMA = query.getContainer()
            CSEAMA.set("HSCONO", currentCompany)
            CSEAMA.set("HSSEA1",  mi.in.get("SEA1"))
            if (!query.read(CSEAMA)) {
                mi.error("Saison " + mi.in.get("SEA1") + " n'existe pas")
                return
            }
        }

        if(mi.in.get("VFDT") != null && mi.in.get("VFDT") != "0") {
            vfdt = mi.in.get("VFDT")
            if (!utility.call("DateUtil", "isDateValid", vfdt, "yyyyMMdd")) {
                mi.error("Date de début de validité est invalide")
                return
            }
        }

        if (mi.in.get("SEQN") != "" && mi.in.get("SEQN") != null)
            seqn = mi.in.get("SEQN")

        DBAction query = database.table("MITPOP").index("00").selection("MPCHNO", "MPCNQT", "MPALUN", "MPREMK").build()
        DBContainer MITPOP = query.getContainer()
        MITPOP.set("MPCONO", currentCompany)
        MITPOP.set("MPALWT", alwt)
        MITPOP.set("MPALWQ", alwq)
        MITPOP.set("MPITNO", itno)
        MITPOP.set("MPPOPN", popn)
        MITPOP.set("MPE0PA", e0pa)
        MITPOP.set("MPSEA1", sea1)
        MITPOP.set("MPVFDT", vfdt as Integer)
        if(!query.readLock(MITPOP, updateCallBack)){
            mi.error("Référence complémentaire n'existe pas")
            return
        }
    }
    Closure<?> updateCallBack = { LockedResult lockedResult ->
        LocalDateTime timeOfCreation = LocalDateTime.now()
        int changeNumber = lockedResult.get("MPCHNO")
        if (mi.in.get("SEQN") != null)
            lockedResult.set("MPSEQN", seqn)
        lockedResult.setInt("MPLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        lockedResult.setInt("MPCHNO", changeNumber + 1)
        lockedResult.set("MPCHID", program.getUser())
        lockedResult.update()
    }
}
