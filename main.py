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
    radius: float = Query(50),  # default 50km if only crime is provided
    location: str = Query(None),
    crime_type: str = Query(None),
    db: Session = Depends(get_db)
):
    try:
        crimes = db.query(models.CrimeReport).all()
        filtered = []

        for crime in crimes:
            crime_loc = (crime.latitude, crime.longitude)
            user_loc = (latitude, longitude) if latitude and longitude else None
            distance_ok = True
            type_ok = True
            location_ok = True

            # Filter by distance only if user location is known (for "only crime" case)
            if crime_type and not location and user_loc:
                distance_ok = geodesic(user_loc, crime_loc).km <= radius

            # Filter by exact coordinates if location is used (assuming app gets lat/lng from Places SDK)
            if location and latitude and longitude:
                distance_ok = geodesic((latitude, longitude), crime_loc).km < 1  # ~1km radius for location match

            if crime_type:
                type_ok = crime.crime_type.lower() == crime_type.lower()

            if crime_type and location:
                # Match both
                if distance_ok and type_ok:
                    filtered.append(crime)
            elif crime_type and not location:
                # Only crime near user location
                if distance_ok and type_ok:
                    filtered.append(crime)
            elif location and not crime_type:
                # Only location match
                if distance_ok:
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

        if crime_type and location:
            # Crime + Location → filter both
            query = query.filter(
                models.CrimeReport.crime_type.ilike(f"%{crime_type}%"),
                models.CrimeReport.description.ilike(f"%{location}%")
            )

        elif location and not crime_type:
            # Location only
            query = query.filter(models.CrimeReport.description.ilike(f"%{location}%"))

        elif crime_type and user_lat is not None and user_lon is not None:
            # Crime only + User Location → find crimes of that type near user
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

        return query.all()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error searching crimes: {str(e)}")

# ✅ Get all crimes
@app.get("/crimes/all", response_model=List[schemas.CrimeReportResponse])
def get_all_crimes(db: Session = Depends(get_db)):
    try:
        return db.query(models.CrimeReport).all()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error fetching all crimes: {str(e)}")
