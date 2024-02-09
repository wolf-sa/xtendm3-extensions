/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT058MI.UpdLngName
 * Description : Update records from the MPDODS table.
 * Date         Changed By   Description
 * 20220621     RENARN       REFX02 - Language name handling
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdLngName extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private String iShortDescription
  private String iDescription

  public UpdLngName(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
    // Check option
    if(mi.in.get("OPTN") != null){
      DBAction query1 = database.table("MPDOPT").index("00").build()
      DBContainer MPDOPT = query1.getContainer()
      MPDOPT.set("PFCONO",currentCompany)
      MPDOPT.set("PFOPTN", mi.in.get("OPTN"))
      if (!query1.read(MPDOPT)) {
        mi.error("Variante " + mi.in.get("OPTN") + " n'existe pas")
        return
      }
    }
    // Check language
    if(mi.in.get("LNCD") != null){
      DBAction query2 = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = query2.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "LNCD")
      CSYTAB.set("CTSTKY", mi.in.get("LNCD"))
      if (!query2.read(CSYTAB)) {
        mi.error("Language " + mi.in.get("LNCD") + " n'existe pas")
        return
      }
    }
    // Check feature
    if(mi.in.get("FTID") != null){
      DBAction query3 = database.table("MPDFHE").index("00").build()
      DBContainer MPDFHE = query3.getContainer()
      MPDFHE.set("PECONO",currentCompany)
      MPDFHE.set("PEFTID", mi.in.get("FTID"))
      if (!query3.read(MPDFHE)) {
        mi.error("Caractéristique " + mi.in.get("FTID") + " n'existe pas")
        return
      }
    }
    iDescription=""
    iShortDescription=""
    if(mi.in.get("TX30") != null)iDescription=mi.in.get("TX30")
    if(mi.in.get("TX15") != null)iShortDescription=mi.in.get("TX15")
    // Update MPDODS
    DBAction query_MPDODS = database.table("MPDODS").index("00").build()
    DBContainer MPDODS = query_MPDODS.getContainer()
    MPDODS.set("QPCONO", currentCompany)
    MPDODS.set("QPOPTN",  mi.in.get("OPTN"))
    MPDODS.set("QPLNCD",  mi.in.get("LNCD"))
    MPDODS.set("QPFTID",  mi.in.get("FTID"))
    if (!query_MPDODS.readLock(MPDODS, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas")

    }

  }
  // Update MPDODS
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("QPCHNO")
    lockedResult.set("QPTX30", iDescription)
    lockedResult.set("QPTX15", iShortDescription)
    if(mi.in.get("TXID") != null){
      lockedResult.set("QPTXID", mi.in.get("TXID"))
    }
    lockedResult.setInt("QPLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("QPCHNO", changeNumber + 1)
    lockedResult.set("QPCHID", program.getUser())
    lockedResult.update()
  }
}
