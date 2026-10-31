#!/bin/sh
# Builds the read-only content database that ContentDatabase ships with, from the original
# app's my504 database. Rerun it when the word list changes, then bump ContentDatabase.VERSION.
#
# usage: tools/build-content-db.sh <path to original my504> <output content.db>
set -eu
SRC="$1"
OUT="$2"
rm -f "$OUT"
mkdir -p "$(dirname "$OUT")"

# Table and index definitions must match the Room entities in core/database exactly
sqlite3 "$OUT" <<SQL
CREATE TABLE classes (id INTEGER NOT NULL, PRIMARY KEY(id));
CREATE TABLE lessons (
  id INTEGER NOT NULL,
  class_id INTEGER NOT NULL,
  number_in_class INTEGER NOT NULL,
  first_word_id INTEGER NOT NULL,
  last_word_id INTEGER NOT NULL,
  PRIMARY KEY(id)
);
CREATE INDEX index_lessons_class_id ON lessons (class_id);
CREATE TABLE words (
  id INTEGER NOT NULL,
  lesson_id INTEGER NOT NULL,
  class_id INTEGER NOT NULL,
  english TEXT NOT NULL,
  persian TEXT NOT NULL,
  synonyms TEXT,
  phonetic TEXT,
  part_of_speech TEXT,
  sentence_en_1 TEXT,
  sentence_fa_1 TEXT,
  sentence_en_2 TEXT,
  sentence_fa_2 TEXT,
  sentence_en_3 TEXT,
  sentence_fa_3 TEXT,
  audio_asset TEXT,
  PRIMARY KEY(id)
);
CREATE INDEX index_words_lesson_id ON words (lesson_id);
CREATE INDEX index_words_class_id ON words (class_id);

ATTACH DATABASE '$SRC' AS legacy;
INSERT INTO classes (id) SELECT class_number FROM legacy.class ORDER BY class_number;
INSERT INTO lessons (id, class_id, number_in_class, first_word_id, last_word_id)
  SELECT lesson_number, class_number, ((lesson_number - 1) % 6) + 1,
         (lesson_number - 1) * 12 + 1, lesson_number * 12
  FROM legacy.lesson ORDER BY lesson_number;
INSERT INTO words
  SELECT word_number, lesson_number, class_number, trim(word_en), trim(word_fa),
         NULLIF(trim(synonyms), ''), NULLIF(trim(phonetic), ''), NULLIF(trim(part_of_speech), ''),
         NULLIF(trim(sentence_en_1), ''), NULLIF(trim(sentence_fa_1), ''),
         NULLIF(trim(sentence_en_2), ''), NULLIF(trim(sentence_fa_2), ''),
         NULLIF(trim(sentence_en_3), ''), NULLIF(trim(sentence_fa_3), ''),
         'audio/' || replace(replace(lower(trim(word_en)), ' ', ''), '-', '') || '.mp3'
  FROM legacy."504" ORDER BY word_number;
DETACH DATABASE legacy;
PRAGMA user_version = 1;
VACUUM;
SQL
echo "classes=$(sqlite3 "$OUT" 'select count(*) from classes') lessons=$(sqlite3 "$OUT" 'select count(*) from lessons') words=$(sqlite3 "$OUT" 'select count(*) from words')"
