package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.progress.ProgressRepository
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import com.bellfamily.bastischool.learning.session.DurableSessionHost

class ColoursHost(storage: ProgressStorage, progress: ProgressRepository) : DurableSessionHost(
    storage, progress, ColoursContent.activity, ColoursContent.REVISION, ColoursContent.repository,
    { id, round, seed -> ColoursContent.generate(id, round, seed) }, ColoursContent::validate
)
