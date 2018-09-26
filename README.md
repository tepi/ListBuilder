[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/listbuilder)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/listbuilder.svg)](https://vaadin.com/directory/component/listbuilder)

ListBuilder
===========

ListBuilder is an evolutionary enhancement of TwinColSelect (and is more or less based on it). I have myself needed this kind of component in a few client projects so I decided to write it under the Apache 2.0 license so others may use it too. 

#####Main enhancements: 
* Item order preserved! 
* Prettier buttons (with icons). 
* Button state is updated based on what is selected. 
* Fixes a few bugs from TwinColSelect which may result into simultaneously selected rows in both columns. 
* Allow adding style names to left and right column captions. 

#####Drawbacks: 
* You need to use the getOrderedValue() method to fetch the selected items in correct order; getValue() will return the selections as a Set similarly to TwinColSelect.

License
=======

Copyright 2014 Teppo Kurki / Vaadin Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
