from pydantic import BaseModel
from datetime import datetime

class CrimeReportBase(BaseModel):
    username: str
    latitude: float
    longitude: float
    crime_type: str
    description: str
    date: datetime

class CrimeReportCreate(CrimeReportBase):
    pass

class CrimeReportResponse(CrimeReportBase):
    id: int

    class Config:
        from_attributes = True
