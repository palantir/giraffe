# -*- coding: utf-8 -*-

import os

from datetime import datetime

import javalink

# -- General configuration ------------------------------------------------
# See http://sphinx-doc.org/config.html for details

extensions = ['javalink']

project = u'Giraffe'
copyright = u'{}, Palantir Technologies'.format(datetime.now().year)

release = os.environ.get('GIRAFFE_VERSION', 'unknown')
if '-' in release:
    version = release.split('-')[0]
else:
    version = release

master_doc = 'index'
source_suffix = '.rst'

exclude_patterns = []
templates_path = ['templates']

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'
highlight_language = 'java'

# Assume javadoc was already build by Gradle
javalink_classpath = [
    javalink.find_rt_jar(),
    '../../core/build/classes/main',
    '../../ssh/build/classes/main',
    '../../fs-base/build/classes/main'
]

javalink_docroots = [
    'http://docs.oracle.com/javase/7/docs/api/',
    ('../build/javadoc', 'api')
]

javalink_add_package_names = False

# -- Options for HTML output ----------------------------------------------
# See http://sphinx-doc.org/config.html#html-options for details

html_static_path = ['static']
html_theme = 'sphinx_rtd_theme'
