/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT860MI.UpdLngTxt
 * Description : Update records to the CSLGHN table.
 * Date         Changed By   Description
 * 20220602     RENARN       REFX01 - Language handling
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdLngTxt extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private long dtid
  private String iDescription
  private String iShortDescription

  public UpdLngTxt(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check language
    if (mi.in.get("LNCD") != null) {
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO", currentCompany)
      CSYTAB.set("CTSTCO", "LNCD")
      CSYTAB.set("CTSTKY", mi.in.get("LNCD"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Language " + mi.in.get("LNCD") + " n'existe pas")
        return
      }
    }
    iDescription =""
    iShortDescription =""
    if(mi.in.get("TX60") != null)iDescription=mi.in.get("TX60")
    if(mi.in.get("TX15") != null)iShortDescription=mi.in.get("TX15")
    // Update CSLGHN
    DBAction query_CSLGHN = database.table("CSLGHN").index("00").build()
    DBContainer CSLGHN = query_CSLGHN.getContainer()
    CSLGHN.set("JLCONO", currentCompany)
    CSLGHN.set("JLDTID", mi.in.get("DTID"))
    CSLGHN.set("JLLNCD", mi.in.get("LNCD"))
    if (!query_CSLGHN.readLock(CSLGHN, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas")

    }

  }
  // Update CSLGHN
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("JLCHNO")
    lockedResult.set("JLTX60", iDescription)
    lockedResult.set("JLTX15", iShortDescription)
    lockedResult.set("JLFILE", mi.in.get("FILE"))
    lockedResult.set("JLFLDI", mi.in.get("FLDI"))
    lockedResult.setInt("JLLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("JLCHNO", changeNumber + 1)
    lockedResult.set("JLCHID", program.getUser())
    lockedResult.update()
  }
}
