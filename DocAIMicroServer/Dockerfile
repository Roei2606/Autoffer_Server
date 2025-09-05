FROM python:3.11-slim

# 1) כלים שנחוצים לעיבוד PDF (השארת poppler-utils - טוב)
RUN apt-get update && \
    apt-get install -y --no-install-recommends poppler-utils && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

# 2) שדרוג pip ו-cache נקי (מקטין תקלות התקנה)
COPY requirements.txt ./
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 3) אפליקציה
COPY app ./app

# 4) איכות חיים בפייתון (אופציונלי)
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# 5) הרצה ל-DEV עם reload (לפרודקשן תוריד את --reload)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]
