from sqlalchemy.orm import Session
import models, schemas

def create_crime_report(db: Session, report: schemas.CrimeReportCreate):
    db_report = models.CrimeReport(**report.dict())
    db.add(db_report)
    db.commit()
    db.refresh(db_report)
    return db_report

def get_crimes(db: Session, skip: int = 0, limit: int = 10):
    return db.query(models.CrimeReport).offset(skip).limit(limit).all()
