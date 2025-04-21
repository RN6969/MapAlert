from fastapi import FastAPI, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from geopy.distance import geodesic
from typing import List

import models, schemas, database
from database import SessionLocal, engine, Base

app = FastAPI()

# ✅ Create all tables if they don't exist
Base.metadata.create_all(bind=engine)

# ✅ Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ✅ Report a crime
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

# ✅ Get nearby crimes (you should add filtering logic later)
@app.get("/crimes/nearby", response_model=List[schemas.CrimeReportBase])
def get_nearby_crimes(
    latitude: float = Query(None),
    longitude: float = Query(None),
    radius: float = Query(50),  # Default to 50km
    location: str = Query(None),
    crime_type: str = Query(None),
    db: Session = Depends(get_db)
):
    try:
        crimes = db.query(models.CrimeReport).all()
        filtered = []

        for crime in crimes:
            match = True

            # ✅ If user location provided, check distance
            if latitude is not None and longitude is not None:
                user_coords = (latitude, longitude)
                crime_coords = (crime.latitude, crime.longitude)
                distance = geodesic(user_coords, crime_coords).km

                # 🔹 Only check radius if we are doing "only crime near me"
                if crime_type and not location:
                    if distance > radius:
                        match = False

                # 🔹 If location is present, match only if it's very close (~1km)
                if location:
                    if distance > 1:  # 1km radius for place match
                        match = False

            # ✅ If crime_type is provided, filter by type
            if crime_type and crime.crime_type.lower() != crime_type.lower():
                match = False

            if match:
                filtered.append(crime)

        return filtered

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error filtering crimes: {str(e)}")

@app.get("/crimes/search", response_model=List[schemas.CrimeReportResponse])
def search_crimes(
    crime_type: str = Query(None),
    location: str = Query(None),
    user_lat: float = Query(None),
    user_lon: float = Query(None),
    db: Session = Depends(get_db)
):
    try:
        query = db.query(models.CrimeReport)

        if crime_type and location and user_lat is not None and user_lon is not None:
            # Crime + Location → match both by type and coordinates (~1km)
            all_crimes = query.filter(models.CrimeReport.crime_type.ilike(f"%{crime_type}%")).all()
            filtered = []
            target_coords = (user_lat, user_lon)

            for crime in all_crimes:
                crime_coords = (crime.latitude, crime.longitude)
                if geodesic(target_coords, crime_coords).km < 1:
                    filtered.append(crime)
            return filtered

        elif location and user_lat is not None and user_lon is not None:
            # Only Location
            all_crimes = query.all()
            filtered = []
            target_coords = (user_lat, user_lon)

            for crime in all_crimes:
                crime_coords = (crime.latitude, crime.longitude)
                if geodesic(target_coords, crime_coords).km < 1:
                    filtered.append(crime)
            return filtered

        elif crime_type and user_lat is not None and user_lon is not None:
            # Only Crime Type + User Location (within 50km)
            all_crimes = query.filter(models.CrimeReport.crime_type.ilike(f"%{crime_type}%")).all()
            filtered = []
            user_coords = (user_lat, user_lon)

            for crime in all_crimes:
                crime_coords = (crime.latitude, crime.longitude)
                if geodesic(user_coords, crime_coords).km <= 50:
                    filtered.append(crime)
            return filtered

        else:
            return []

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error searching crimes: {str(e)}")


# ✅ Get all crimes
@app.get("/crimes/all", response_model=List[schemas.CrimeReportResponse])
def get_all_crimes(db: Session = Depends(get_db)):
    try:
        return db.query(models.CrimeReport).all()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error fetching all crimes: {str(e)}")
