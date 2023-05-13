class Word:
    def __init__(self, word: str,
                 definition : str,
                 difficulty: int):
        self._word = word
        self._definition = definition
        self._difficulty = difficulty

    def __getitem__(self, item):
        return self._word[item]

    @property
    def get_size(self) -> int:
        return len(self._word)

    @property
    def get_difficulty(self)  -> str:
        return self._difficulty

