# Base image
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Copy code and install dependencies
COPY . /app
RUN pip install --no-cache-dir -r requirements.txt


# Command to run FastAPI
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
