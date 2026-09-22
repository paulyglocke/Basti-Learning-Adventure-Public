package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.DurableSessionHost

/** Existing Prepositions journal path, schema and behavior are unchanged. */
class PrepositionsHost(journal: ProgressStorage, progress: ProgressRepository) : DurableSessionHost(
    journal, progress, PrepositionsContent.activity, PrepositionsContent.REVISION, PrepositionsContent.repository,
    { id, round, seed -> PrepositionsContent.generate(id, round, seed) }, PrepositionsContent::validate)
