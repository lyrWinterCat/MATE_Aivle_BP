from sqlalchemy import Column, Integer, String, ForeignKey, Text, Enum, DateTime, Boolean
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
import enum

Base = declarative_base()

class roleEnum(enum.Enum):
    USER = "USER"
    ADMIN = "ADMIN"

class Summary(Base):
    __tablename__ = "summary"
    
    summary_id = Column(Integer, primary_key=True, index=True)
    meeting_id = Column(Integer, nullable=False)
    summary_text = Column(Text)
    
class User(Base):
    __tablename__ = "user"
    
    user_id = Column(Integer, primary_key=True, index=True)
    department_id = Column(Integer)
    email = Column(String(255))
    password = Column(String(255))
    name = Column(String(255))
    role = Column(Enum(roleEnum))
    create_at = Column(DateTime)
    update_at = Column(DateTime)
    is_social = Column(Boolean, server_default=0)
    authorized = Column(Boolean, server_default=0)

class SpeechLog(Base):
    __tablename__ = "speech_log"
    
    log_id = Column(Integer, primary_key=True)
    meeting_id = Column(Integer)
    user_id = Column(Integer)
    timestamp = Column(DateTime)
    is_off_topic = Column(Boolean, server_default=1)
    
class ToxicityLog(Base):
    __tablename__ = "toxicity_log"
    
    toxicity_id = Column(Integer, primary_key=True)
    log_id = Column(Integer)
    meeting_id = Column(Integer)
    user_id = Column(Integer)
    corrected = Column(Boolean, server_default=1)
    updated_at = Column(DateTime)
    
    