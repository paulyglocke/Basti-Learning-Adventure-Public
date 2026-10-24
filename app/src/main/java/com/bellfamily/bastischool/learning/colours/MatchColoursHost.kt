package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.progress.ProgressRepository
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import com.bellfamily.bastischool.learning.session.DurableSessionHost

class MatchColoursHost(storage: ProgressStorage, progress: ProgressRepository) : DurableSessionHost(
    storage, progress, MatchColoursContent.activity, MatchColoursContent.REVISION,
    MatchColoursContent.repository, MatchColoursContent::generate, MatchColoursContent::validate
)
