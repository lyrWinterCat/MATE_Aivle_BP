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
    
class Meeting(Base):
    __tablename__ = "meeting"
    
    meeting_id = Column(Integer, primary_key=True)
    meeting_name = Column(String(100))
    end_time = Column(DateTime)
    created_at = Column(DateTime)
    url = Column(Text)
    filepath = Column(Text)
    last_break_time = Column(DateTime)
    
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
    is_social = Column(Boolean)
    authorized = Column(Boolean)

class SpeechLog(Base):
    __tablename__ = "speech_log"
    
    log_id = Column(Integer, primary_key=True)
    meeting_id = Column(Integer)
    user_id = Column(Integer)
    timestamp = Column(DateTime)
    content = Column(Text)
    
class ToxicityLog(Base):
    __tablename__ = "toxicity_log"
    
    toxicity_id = Column(Integer, primary_key=True)
    log_id = Column(Integer)
    meeting_id = Column(Integer)
    user_id = Column(Integer)
    corrected = Column(Boolean)
    updated_at = Column(DateTime)
    
    