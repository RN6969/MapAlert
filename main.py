from fastapi import FastAPI, Depends, HTTPException, Query
from sqlalchemy.orm import Session
import models, schemas, database
from typing import List

from database import SessionLocal

app = FastAPI()

def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/crimes/report", response_model=schemas.CrimeReportBase)
def report_crime(crime: schemas.CrimeReportCreate, db: Session = Depends(get_db)):
    try:
        new_crime = models.CrimeReport(**crime.dict())
        db.add(new_crime)
        db.commit()
        db.refresh(new_crime)
        return new_crime
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error reporting crime: {str(e)}")

@app.get("/crimes/nearby", response_model=List[schemas.CrimeReportBase])
def get_nearby_crimes(
    latitude: float = Query(...),
    longitude: float = Query(...),
    radius: float = Query(...)
):
    db = SessionLocal()
    try:
        crimes = db.query(models.CrimeReport).all()
        return crimes
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error fetching crimes: {str(e)}")
    finally:
        db.close()

@app.get("/crimes/all", response_model=List[schemas.CrimeReportResponse])
def get_all_crimes(db: Session = Depends(get_db)):
    try:
        return db.query(models.CrimeReport).all()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error fetching all crimes: {str(e)}")
