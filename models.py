from sqlalchemy import Column, Integer, String, Float, DateTime
from database import Base
from datetime import datetime

class CrimeReport(Base):
    __tablename__ = "crime_reports"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    crime_type = Column(String(50), nullable=False)
    description = Column(String(255), nullable=True)
    date = Column(DateTime, default=datetime.utcnow)
