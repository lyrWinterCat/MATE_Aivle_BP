from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker, Session
import asyncio
import ssl

from sqlalchemy import create_engine

import os 

with open("DB_URL.txt", "r") as f:
    DATABASE_URL = f.readline()

## 동기
# engine = create_engine(DATABASE_URL, echo=True)

# SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# def get_db():
#     db = SessionLocal()
#     try:
#         yield db
#     finally:
#         db.close()

ssl_ctx= ssl.create_default_context(cafile="./DigiCertGlobalRootCA.crt.pem")
ssl_ctx.verify_mode = ssl.CERT_REQUIRED
# 비동기 
engine = create_async_engine(
    DATABASE_URL, 
    echo=True, 
    future=True, 
    connect_args={
        # "ssl": {"ca": "./DigiCertGlobalRootCA.crt.pem"}
        "ssl": ssl_ctx
    })

AsyncSessionLocal = sessionmaker(bind=engine, class_=AsyncSession, expire_on_commit=False)

async def get_db():
    async with AsyncSessionLocal() as session:
        yield session